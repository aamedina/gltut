(ns gltut.tut07.positioning
  (:require [lwcgl.core :refer [defsketch]]
            [lwcgl.opengl.v41 :refer :all]
            [lwcgl.input.keyboard :as kb]
            [lwcgl.buffers :as buffers]
            [lwcgl.math :refer :all :exclude [min max]]
            
            [euclidean.math.matrix :as m]
            [euclidean.math.vector :as v]

            [gltut.shader :as shader :refer [program]]
            [gltut.mesh :as mesh :refer [mesh]]
            [gltut.opengl :as gl :refer :all]
            [gltut.util :as util :refer [key-presses]]))

(def uniforms
  [:model-to-world-matrix
   :world-to-camera-matrix
   :camera-to-clip-matrix
   :base-color])

(defn setup
  []
  
  (gl-enable GL_CULL_FACE)
  (gl-cull-face GL_BACK)
  (gl-front-face GL_CW)

  (gl-enable GL_DEPTH_TEST)
  (gl-depth-mask true)
  (gl-depth-func GL_LEQUAL)
  (gl-depth-range 0.0 1.0)
  (gl-enable GL_DEPTH_CLAMP)
  
  {:meshes {:cone-mesh (mesh "unit-cone-tint.edn")
            :cylinder-mesh (mesh "unit-cylinder-tint.edn")
            :cube-tint-mesh (mesh "unit-cube-tint.edn")
            :cube-color-mesh (mesh "unit-cube-color.edn")
            :plane-mesh (mesh "unit-plane.edn")}
   :programs {:uniform-color (program "tut07/PosOnlyWorldTransform.vert"
                                      "tut07/ColorUniform.frag" uniforms)
              :object-color (program "tut07/PosColorWorldTransform.vert"
                                     "tut07/ColorPassthrough.frag" uniforms)
              :uniform-color-tint (program "tut07/PosColorWorldTransform.vert"
                                           "tut07/ColorMultUniform.frag"
                                           uniforms)}
   :z-near 1.0
   :z-far 1000.0
   :buffer (buffers/create-float-buffer 16)
   :draw-lookat-point false
   :cam-target #math/vector [0.0 0.4 0.0]
   :sphere-cam-rel-pos #math/vector [67.5 -46.0 150.0]})

(defn camera-position
  [target [x y z :as rel-pos]]
  (let [phi (to-radians x)
        theta (to-radians (+ y 90.0))]
    (v/add (v/scale (v/vector (* (sin theta) (cos phi)) (cos theta)
                              (* (sin theta) (sin phi))) z) target)))

(defn close-on-escape
  [state]
  (cond-> state
    (kb/key-down? kb/KEY_ESCAPE) (assoc :finished? true)))

(defn update
  [state]
  (-> state
      close-on-escape))

(defn draw
  [state]
  (gl-clear-color 0 0 0 0)
  (gl-clear-depth 1.0)
  (gl-clear (bit-or GL_COLOR_BUFFER_BIT GL_DEPTH_BUFFER_BIT))

  (let [camera-matrix (camera-position (:cam-target state)
                                       (:sphere-cam-rel-pos state))
        {:keys [uniform-color]} (:programs state)]
    (with-matrix-stack camera-matrix
      (with-program (assoc uniform-color
                      :location :world-to-camera-matrix)
        ))))

(defsketch world-scene
  :setup setup
  :update update
  :draw draw
  :size [700 700])
