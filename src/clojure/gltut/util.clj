(ns gltut.util
  (:require [lwcgl.core :refer [sketch]]
            [lwcgl.buffers :as buffers]
            [lwcgl.openal :as al]
            [lwcgl.sys :as sys]
            [lwcgl.util.glu :refer :all]
            
            [lwcgl.opengl :refer :all]
            [lwcgl.opengl.v41 :refer :all]
            [lwcgl.opengl.display :as d :refer [width height]]            
            
            [lwcgl.input.keyboard :as kb]
            [lwcgl.input.mouse :as mouse]
            
            [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix2d :as mat2]
            [lwcgl.math.matrix3d :as mat3]
            [lwcgl.math.matrix4d :as mat4]
            [lwcgl.math.vector2d :as vec2]
            [lwcgl.math.vector3d :as vec3]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]
            [lwcgl.math.quaternion :as q]
            
            [clojure.java.io :as io]

            [riddley.walk :as walk])
  (:import (java.nio.file FileSystem StandardWatchEventKinds LinkOption
                          FileSystems StandardWatchEventKinds$StdWatchEventKind)
           (java.nio.file.attribute FileAttribute)))

(defn print-info
  [ns]
  (println)
  (println "-----------------------------------------------------------")
  (printf "%-18s%s\n" "Running:" (ns-name ns))
  (printf "%-18s%s\n" "OpenGL version:" (gl-get-string GL_VERSION))
  (when-not (:OpenGL33 (gl-capabilities))
    (println "You must have at least OpenGL 3.3 to run this tutorial."))
  (flush))

(defn handle-shader-error
  [shader-type shader]
  (let [status (gl-get-shaderi shader GL_COMPILE_STATUS)]
    (when (== status GL_FALSE)
      (let [info-log (->> (gl-get-shaderi shader GL_INFO_LOG_LENGTH)
                          (gl-get-shader-info-log shader))
            type (condp == shader-type
                   GL_VERTEX_SHADER "vertex"
                   GL_FRAGMENT_SHADER "fragment")]
        (binding [*out* *err*]
          (printf "Compile failure in %s shader:\n%s\n" type info-log))))))

(defn handle-program-error
  [program]
  (let [status (gl-get-programi program GL_LINK_STATUS)]
    (when (== status GL_FALSE)
      (let [info-log (->> (gl-get-shaderi program GL_INFO_LOG_LENGTH)
                          (gl-get-shader-info-log program))]
        (binding [*out* *err*]
          (printf "Linker failure: %s\n" info-log))))))

(defn create-shader
  [shader-type shader-src]
  (let [shader (gl-create-shader shader-type)]
    (gl-shader-source shader shader-src)
    (gl-compile-shader shader)
    (handle-shader-error shader-type shader)
    shader))

(defn create-program
  [vert frag]
  (let [program (doto (gl-create-program)
                  (gl-attach-shader vert)
                  (gl-attach-shader frag)
                  (gl-link-program))]
    (handle-program-error program)
    (doseq [shader [vert frag]]
      (gl-detach-shader program shader))
    program))

(defn init-program
  [{:keys [vert frag] :as state}]
  (let [vert (create-shader GL_VERTEX_SHADER (slurp (io/resource vert)))
        frag (create-shader GL_FRAGMENT_SHADER (slurp (io/resource frag)))
        the-program (create-program vert frag)]
    (doseq [shader [vert frag]]
      (gl-delete-shader shader))
    (assoc state
      :the-program the-program)))

(definline buffer-of
  [t element-array]
  `(doto (case ~t
           :byte (buffers/create-byte-buffer (alength ~element-array))
           :short (buffers/create-short-buffer (alength ~element-array))
           :int (buffers/create-int-buffer (alength ~element-array))
           :long (buffers/create-long-buffer (alength ~element-array))
           :float (buffers/create-float-buffer (alength ~element-array))
           :double (buffers/create-double-buffer (alength ~element-array)))
     (.put ~element-array)
     (.flip)))

(defn path
  [file-path]
  (.getPath (FileSystems/getDefault) file-path (into-array String [])))

(defn register
  [watcher path event]
  (let [event (case event
                :create java.nio.file.StandardWatchEventKinds/ENTRY_CREATE
                :delete java.nio.file.StandardWatchEventKinds/ENTRY_DELETE
                :modify java.nio.file.StandardWatchEventKinds/ENTRY_MODIFY)
        vargs (into-array StandardWatchEventKinds$StdWatchEventKind [event])
        key (.register path watcher vargs)]
    key))

(def file-watcher (.newWatchService (java.nio.file.FileSystems/getDefault)))

(defmacro with-vertex-attrib-arrays
  [ns & body]
  `(do (doseq [n# ~ns]
         (gl-enable-vertex-attrib-array n#))
       ~@body
       (doseq [n# ~ns]
         (gl-disable-vertex-attrib-array n#))))

(defmacro with-program
  [program & body]
  `(do (gl-use-program ~program)
       ~@body
       (gl-use-program 0)))

(defn init-vertex-buffer
  [state vertex-data]
  (assoc state
    :vertex-positions-buffer (buffer-of :float vertex-data)))

(defn init-position-buffer-object
  [{:keys [vertex-positions-buffer] :as state}]
  (let [pbo (gl-gen-buffers)]
    (gl-bind-buffer GL_ARRAY_BUFFER pbo)
    (gl-buffer-data GL_ARRAY_BUFFER vertex-positions-buffer GL_STATIC_DRAW)
    (gl-bind-buffer GL_ARRAY_BUFFER 0)
    (assoc state
      :position-buffer-object pbo)))

(defn gen-buffers
  [t data usage]
  (let [array (case t
                :byte (byte-array data)
                :short (short-array data)
                :int (int-array data)
                :long (long-array data)
                :float (float-array data)
                :double (double-array data))
        data (buffer-of t array)
        vertex-buffer-object (gl-gen-buffers)]
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (gl-buffer-data GL_ARRAY_BUFFER data usage)
    (gl-bind-buffer GL_ARRAY_BUFFER 0)
    vertex-buffer-object))

(defn create-matrix
  [mat]
  (buffer-of :float mat))

(defmacro with-vertex-array
  [vertex-array & body]
  (let [has-more? (atom false)]
    (doseq [form body]
      (walk/walk-exprs #(and (symbol? %) (= (resolve %) #'with-vertex-array))
                       (fn [_] (reset! has-more? true))
                       '#{with-vertex-array gltut.util/with-vertex-array}
                       form))
    (if @has-more?
      `(do (gl-bind-vertex-array ~vertex-array)
           ~@body)
      `(let [ret# (do (gl-bind-vertex-array ~vertex-array)
                      ~@body)]
         (gl-bind-vertex-array 0)
         ret#))))

(defn key-event
  []
  (when (kb/next)
    (kb/event-key)))

(defn key-pressed?
  [key]
  (kb/event-key-state))

(defn key-released?
  [key]
  (not (key-pressed? key)))

(defn key-events
  []
  (take-while (complement nil?) (repeatedly key-event)))

(defn key-presses
  []
  (filter key-pressed? (key-events)))

(defn key-releases
  []
  (filter key-released? (key-events)))

(defn mat-to-array
  [mat]
  (float-array [(.-m00 mat) (.-m10 mat) (.-m20 mat) (.-m30 mat)
                (.-m01 mat) (.-m11 mat) (.-m21 mat) (.-m31 mat)
                (.-m02 mat) (.-m12 mat) (.-m22 mat) (.-m32 mat)
                (.-m03 mat) (.-m13 mat) (.-m23 mat) (.-m33 mat)]))

(defprotocol Mix
  (mix [x y a]))

(extend-protocol Mix
  Number
  (mix [x y a]
    (+ (* x (- 1 a)) (* y a)))
  
  org.lwjgl.util.vector.Vector4f
  (mix [x y a]
    (vec4/add x (vec4/scale (vec4/sub y x (vec4)) a) (vec4))))

(defn from-mat2
  [mat]
  [(.-m00 mat) (.-m10 mat)
   (.-m01 mat) (.-m11 mat)])

(defn from-mat3
  [mat]
  [(.-m00 mat) (.-m10 mat) (.-m20 mat)
   (.-m01 mat) (.-m11 mat) (.-m21 mat)
   (.-m02 mat) (.-m12 mat) (.-m22 mat)])

(defn from-mat4
  [mat]
  [(.-m00 mat) (.-m10 mat) (.-m20 mat) (.-m30 mat)
   (.-m01 mat) (.-m11 mat) (.-m21 mat) (.-m31 mat)
   (.-m02 mat) (.-m12 mat) (.-m22 mat) (.-m32 mat)
   (.-m03 mat) (.-m13 mat) (.-m23 mat) (.-m33 mat)])

(defn clamp
  [val min max]
  (cond
    (< val min) min
    (> val max) max
    :else val))
