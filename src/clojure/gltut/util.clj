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
            [lwcgl.math.vector4d :as vec4]
            [lwcgl.math.quaternion :as q]
            [clojure.java.io :as io]))

(defn print-info
  [ns]
  (println)
  (println "-----------------------------------------------------------")
  (printf "%-18s%s\n" "Running:" (ns-name ns))
  (printf "%-18s%s\n" "OpenGL version:" (gl-get-string GL_VERSION))
  (when-not (:OpenGL33 *capabilities*)
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
           :int (buffers/create-int-buffer (alength ~element-array))
           :long (buffers/create-long-buffer (alength ~element-array))
           :float (buffers/create-float-buffer (alength ~element-array))
           :double (buffers/create-double-buffer (alength ~element-array)))
     (.put ~element-array)
     (.flip)))
