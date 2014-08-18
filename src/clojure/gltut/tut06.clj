(ns gltut.tut06
  (:require [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]))

(defprotocol Instance
  (calc-offset [this elapsed-time]))

(defn construct-matrix
  [instance elapsed-time]
  (let [the-matrix (mat4)
        offset-vec (calc-offset instance elapsed-time)]
    (mat4/set-column! the-matrix 3 (vec4 (vec3/x offset-vec)
                                         (vec3/y offset-vec)
                                         (vec3/z offset-vec) 1.0))))

(defn calculate-frustum-scale
  [fov]
  (float (/ 1.0 (tan (/ (to-radians fov) 2.0)))))

(def ^:const frustum-scale (calculate-frustum-scale 45.0))
(def ^:const num-vertices 8)

(def stationary-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      (vec3 0.0 0.0 -20.0))))

(def oval-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      (let [loop-duration 3.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)]
        (vec3 (* (cos (* curr-time-through-loop scale)) 4)
              (* (sin (* curr-time-through-loop scale)) 6)
              -20.0)))))

(def bottom-circle-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      (let [loop-duration 12.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)]
        (vec3 (* (cos (* curr-time-through-loop scale)) 5)
              -3.5
              (- (sin (* curr-time-through-loop scale 5)) 20.0))))))

(def instance-list [stationary-offset oval-offset bottom-circle-offset])

(def vertex-data
  [+1.0 +1.0 +1.0
   -1.0 -1.0 +1.0
   -1.0 +1.0 -1.0
   +1.0 -1.0 -1.0

   -1.0 -1.0 -1.0
   +1.0 +1.0 -1.0
   +1.0 -1.0 +1.0
   -1.0 +1.0 +1.0

   0.0 1.0 0.0 1.0     ;; GREEN_COLOR
   0.0 0.0 1.0 1.0     ;; BLUE_COLOR
   1.0 0.0 0.0 1.0     ;; RED_COLOR
   0.5 0.5 0.0 1.0     ;; BROWN_COLOR

   0.0 1.0 0.0 1.0     ;; GREEN_COLOR
   0.0 0.0 1.0 1.0     ;; BLUE_COLOR
   1.0 0.0 0.0 1.0     ;; RED_COLOR
   0.5 0.5 0.0 1.0     ;; BROWN_COLOR
   ])

(def index-data
  [0, 1, 2,
   1, 0, 3,
   2, 3, 0,
   3, 2, 1,

   5, 4, 6,
   4, 5, 7,
   7, 6, 4,
   6, 7, 5])
