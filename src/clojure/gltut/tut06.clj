(ns gltut.tut06
  (:require [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]))

(definline set-matrix!
  [mat row column val]
  `(case ~[row column]
     [0 0] (set! (.-m00 ~mat) ~val)
     [0 1] (set! (.-m10 ~mat) ~val)
     [0 2] (set! (.-m20 ~mat) ~val)
     [0 3] (set! (.-m30 ~mat) ~val)
     [1 0] (set! (.-m01 ~mat) ~val)
     [1 1] (set! (.-m11 ~mat) ~val)
     [1 2] (set! (.-m21 ~mat) ~val)
     [1 3] (set! (.-m31 ~mat) ~val)
     [2 0] (set! (.-m02 ~mat) ~val)
     [2 1] (set! (.-m12 ~mat) ~val)
     [2 2] (set! (.-m22 ~mat) ~val)
     [2 3] (set! (.-m32 ~mat) ~val)
     [3 0] (set! (.-m03 ~mat) ~val)
     [3 1] (set! (.-m13 ~mat) ~val)
     [3 2] (set! (.-m23 ~mat) ~val)
     [3 3] (set! (.-m33 ~mat) ~val)))

(defn set-row!
  [mat row vec]
  (set-matrix! mat row 0 (vec4/x vec))
  (set-matrix! mat row 1 (vec4/y vec))
  (set-matrix! mat row 2 (vec4/z vec))
  (set-matrix! mat row 3 (vec4/w vec))
  mat)

(defn set-column!
  [mat column vec]
  (set-matrix! mat 0 column (vec4/x vec))
  (set-matrix! mat 1 column (vec4/y vec))
  (set-matrix! mat 2 column (vec4/z vec))
  (set-matrix! mat 3 column (vec4/w vec))
  mat)

(defprotocol Instance
  (calc-offset [this elapsed-time]))

(defn construct-matrix
  [instance elapsed-time]
  (let [the-matrix (mat4)
        offset-vec (calc-offset instance elapsed-time)]
    (vec4 (vec4/x offset-vec) (vec4/y offset-vec) (vec4/z offset-vec) 1.0)))

(defn calculate-frustum-scale
  [fov]
  (float (/ 1.0 (tan (/ (to-radians fov) 2.0)))))

(def ^:const frustum-scale (calculate-frustum-scale 45.0))
(def ^:const num-vertices 8)

(def stationary-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      )))

(def oval-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      )))

(def bottom-circle-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      )))

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
