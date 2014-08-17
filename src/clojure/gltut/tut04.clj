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

(defn setup
  []
  (print-info *ns*)
  (let [vertex-data [0.25 0.25 -1.25 1.0
                     0.25 -0.25 -1.25 1.0
                     -0.25 0.25 -1.25 1.0

                     0.25 -0.25 -1.25 1.0
                     -0.25 -0.25 -1.25 1.0
                     -0.25 0.25 -1.25 1.0

                     0.25 0.25 -2.75 1.0
                     -0.25 0.25 -2.75 1.0
                     0.25 -0.25 -2.75 1.0

                     0.25 -0.25 -2.75 1.0
                     -0.25 0.25 -2.75 1.0
                     -0.25 -0.25 -2.75 1.0

                     -0.25 0.25 -1.25 1.0
                     -0.25 -0.25 -1.25 1.0
                     -0.25 -0.25 -2.75 1.0

                     -0.25 0.25 -1.25 1.0
                     -0.25 -0.25 -2.75 1.0
                     -0.25 0.25 -2.75 1.0

                     0.25 0.25 -1.25 1.0
                     0.25 -0.25 -2.75 1.0
                     0.25 -0.25 -1.25 1.0

                     0.25 0.25 -1.25 1.0
                     0.25 0.25 -2.75 1.0
                     0.25 -0.25 -2.75 1.0

                     0.25 0.25 -2.75 1.0
                     0.25 0.25 -1.25 1.0
                     -0.25 0.25 -1.25 1.0

                     0.25 0.25 -2.75 1.0
                     -0.25 0.25 -1.25 1.0
                     -0.25 0.25 -2.75 1.0

                     0.25 -0.25 -2.75 1.0
                     -0.25 -0.25 -1.25 1.0
                     0.25 -0.25 -1.25 1.0

                     0.25 -0.25 -2.75 1.0
                     -0.25 -0.25 -2.75 1.0
                     -0.25 -0.25 -1.25 1.0


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
                                         {:vert "tut04/MatrixPerspective.vert"
                                          :frag "tut04/StandardColors.frag"})
        state (assoc state
                :start-time (System/nanoTime)
                :vertex-data vertex-data
                :vertex-buffer-object (gen-buffers vertex-data GL_STATIC_DRAW)
                :offset-uniform (gl-get-uniform-location the-program "offset"))
        p-matrix (gl-get-uniform-location the-program "perspectiveMatrix")
        frustum-scale 1.0
        z-near 0.5
        z-far 3.0
        the-matrix [frustum-scale 0 0 0
                    0 frustum-scale 0 0
                    0 0 (/ (+ z-far z-near) (- z-near z-far)) -1.0
                    0 0 (/ (* z-far z-near 2) (- z-near z-far)) 0]
        matrix-array (float-array the-matrix)
        matrix-buffer (buffer-of :float matrix-array)]
    (with-program the-program
      (gl-uniform-matrix4 p-matrix false matrix-buffer))
    (gl-bind-vertex-array (gl-gen-vertex-arrays))
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
  #_(with-program the-program
    (gl-uniform2f offset-uniform 0.5 0.5)
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
