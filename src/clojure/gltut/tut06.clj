(ns gltut.tut06
  (:require [lwcgl.math :refer :all :exclude [min max]]))

(defn calculate-frustum-scale
  [fov]
  (float (/ 1.0 (tan (/ (to-radians fov) 2.0)))))

(def ^:const frustum-scale (calculate-frustum-scale 45.0))
(def ^:const num-vertices 8)

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
