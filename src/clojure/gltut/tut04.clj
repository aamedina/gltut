(ns gltut.tut04
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
            
            [clojure.java.io :as io]

            [gltut.util :refer :all]
            [gltut.tut01 :as tut01]))

(defn compute-position-offsets
  [{:keys [elapsed-time] :as state}]
  (let [loop-duration 5.0
        scale (/ (* 3.14159 2.0) loop-duration)
        elapsed-time (/ elapsed-time 1000.0)
        t (mod elapsed-time loop-duration)]
    (assoc state
      :x-offset (cos (* t scale 0.5))
      :y-offset (sin (* t scale 0.5)))))

(defn adjust-vertex-data
  [{:keys [vertex-buffer-object vertex-data x-offset y-offset] :as state}]
  (let [new-vertex-data (reduce-kv (fn [coll idx val]
                                     (if (zero? (mod idx 4))
                                       (-> coll
                                           (update-in [idx] + x-offset)
                                           (update-in [(inc idx)] + y-offset))
                                       coll))
                                   vertex-data vertex-data)
        buf (buffer-of :float (float-array new-vertex-data))]
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (gl-buffer-sub-data GL_ARRAY_BUFFER 0 buf)
    (gl-bind-buffer GL_ARRAY_BUFFER 0)
    (assoc state
      :vertex-buffer-object vertex-buffer-object)))

(defn update-program
  [state]
  (let [{:keys [the-program] :as new-state} (init-program state)]
    (gl-use-program the-program)
    (gl-uniform1f (:loop-duration state) 5.0)
    (gl-uniform1f (:frag-loop state) 10.0)
    (gl-use-program 0)
    new-state))

(defn setup
  []
  (print-info *ns*)
  (let [vertex-data [0.25 0.25 0.75 1.0
                     0.25 -0.25 0.75 1.0
                     -0.25 0.25 0.75 1.0

                     0.25 -0.25 0.75 1.0
                     -0.25 -0.25 0.75 1.0
                     -0.25 0.25 0.75 1.0
                     
                     0.25 0.25 -0.75 1.0
                     -0.25 0.25 -0.75 1.0
                     0.25 -0.25 -0.75 1.0

                     0.25 -0.25 -0.75 1.0
                     -0.25 0.25 -0.75 1.0
                     -0.25 -0.25 -0.75 1.0

                     -0.25 0.25 0.75 1.0
                     -0.25 -0.25 0.75 1.0
                     -0.25 -0.25 -0.75 1.0

                     -0.25 0.25 0.75 1.0
                     -0.25 -0.25 -0.75 1.0
                     -0.25 0.25 -0.75 1.0

                     0.25 0.25 0.75 1.0
                     0.25 -0.25 -0.75 1.0
                     0.25 -0.25 0.75 1.0

                     0.25 0.25 0.75 1.0
                     0.25 0.25 -0.75 1.0
                     0.25 -0.25 -0.75 1.0

                     0.25 0.25 -0.75 1.0
                     0.25 0.25 0.75 1.0
                     -0.25 0.25 0.75 1.0

                     0.25 0.25 -0.75 1.0
                     -0.25 0.25 0.75 1.0
                     -0.25 0.25 -0.75 1.0

                     0.25 -0.25 -0.75 1.0
                     -0.25 -0.25 0.75 1.0
                     0.25 -0.25 0.75 1.0

                     0.25 -0.25 -0.75 1.0
                     -0.25 -0.25 -0.75 1.0
                     -0.25 -0.25 0.75 1.0


                     0.0 0.0 1.0 1.0
                     0.0 0.0 1.0 1.0
                     0.0 0.0 1.0 1.0

                     0.0 0.0 1.0 1.0
                     0.0 0.0 1.0 1.0
                     0.0 0.0 1.0 1.0

                     0.8 0.8 0.8 1.0
                     0.8 0.8 0.8 1.0
                     0.8 0.8 0.8 1.0

                     0.8 0.8 0.8 1.0
                     0.8 0.8 0.8 1.0
                     0.8 0.8 0.8 1.0

                     0.0 1.0 0.0 1.0
                     0.0 1.0 0.0 1.0
                     0.0 1.0 0.0 1.0

                     0.0 1.0 0.0 1.0
                     0.0 1.0 0.0 1.0
                     0.0 1.0 0.0 1.0

                     0.5 0.5 0.0 1.0
                     0.5 0.5 0.0 1.0
                     0.5 0.5 0.0 1.0

                     0.5 0.5 0.0 1.0
                     0.5 0.5 0.0 1.0
                     0.5 0.5 0.0 1.0

                     1.0 0.0 0.0 1.0
                     1.0 0.0 0.0 1.0
                     1.0 0.0 0.0 1.0

                     1.0 0.0 0.0 1.0
                     1.0 0.0 0.0 1.0
                     1.0 0.0 0.0 1.0

                     0.0 1.0 1.0 1.0
                     0.0 1.0 1.0 1.0
                     0.0 1.0 1.0 1.0

                     0.0 1.0 1.0 1.0
                     0.0 1.0 1.0 1.0
                     0.0 1.0 1.0 1.0]
        {:keys [the-program] :as state} (init-program
                                         {:vert "tut04/OrthoWithOffset.vert"
                                          :frag "tut04/StandardColors.frag"})
        state
        (assoc state
          :start-time (System/nanoTime)
          :vertex-data vertex-data
          :vertex-buffer-object (gen-buffers vertex-data GL_STATIC_DRAW)
          :offset-uniform (gl-get-uniform-location the-program "offset")
          :vao (doto (gl-gen-vertex-arrays)
                 (gl-bind-vertex-array)))]
    (gl-enable GL_CULL_FACE)
    (gl-cull-face GL_BACK)
    (gl-front-face GL_CW)
    state))

(defn update
  [{:keys [start-time last-frame-timestamp the-program] :as state}]
  (let [elapsed-time (/ (- (System/nanoTime) start-time) (float 1000000.0))
        now (System/nanoTime)
        last-frame-duration (/ (- now (or last-frame-timestamp 0))
                               (float 1000000.0))]
    (assoc state
      :elapsed-time elapsed-time
      :last-frame-duration last-frame-duration
      :last-frame-timestamp now
      :finished? (kb/key-down? kb/KEY_ESCAPE))))

(defn draw
  [{:keys [vertex-buffer-object the-program offset-uniform vertex-data]
    :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (with-program the-program
    (gl-uniform2f offset-uniform 0.5 0.25)
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (with-vertex-attrib-arrays [0 1]
      (let [color-data (/ (* 4 (count vertex-data)) 2)]
        (gl-vertex-attrib-pointer 0 4 GL_FLOAT false 0 0)
        (gl-vertex-attrib-pointer 1 4 GL_FLOAT false 0 color-data)
        (gl-draw-arrays GL_TRIANGLES 0 36)))))

(defn tut04
  []
  (when-not (d/created?)
    (future
      (sketch
       :setup (var setup)
       :update (var update)
       :draw (var draw)
       :dispose (var tut01/dispose)
       :frame-rate 60
       :size [500 500]
       :render-in-background? true
       :title "tut04"))))
