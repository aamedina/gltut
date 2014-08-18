(ns gltut.tut06
  (:require [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix2d :as mat2 :refer [mat2]]
            [lwcgl.math.matrix3d :as mat3 :refer [mat3]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]
            [gltut.util :refer [mat-to-array]])
  (:import (java.nio Buffer FloatBuffer)
           (org.lwjgl.util.vector Matrix2f Matrix3f Matrix4f)))

(defprotocol Instance
  (calc-offset [this elapsed-time]))

(defprotocol BufferableData
  (fill-buffer [this ^Buffer buffer]))

(defn construct-matrix
  [instance elapsed-time]
  (let [the-matrix (mat4)
        offset-vec (calc-offset instance elapsed-time)]
    (mat4/set-row! the-matrix 3 (vec4 (vec3/x offset-vec)
                                      (vec3/y offset-vec)
                                      (vec3/z offset-vec) 1.0))))

(defn fill-and-flip-buffer
  [x ^Buffer buffer]
  (.clear buffer)
  (fill-buffer x buffer)
  (.flip buffer)
  buffer)

(extend-protocol BufferableData
  Matrix2f
  (fill-buffer [mat buffer]
    (mat2/store-transpose mat buffer)
    buffer)
  
  Matrix3f
  (fill-buffer [mat buffer]
    (mat3/store-transpose mat buffer)
    buffer)
  
  Matrix4f
  (fill-buffer [mat buffer]
    (mat4/store-transpose mat buffer)
    buffer))

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
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vec3 (* (cos t) 4.0) (* (sin t) 6.0) -20.0)))))

(def bottom-circle-offset
  (reify Instance
    (calc-offset [this elapsed-time]
      (let [loop-duration 12.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vec3 (* (cos t) 5.0) -3.5 (- (* (sin t) 5.0) 20.0))))))

(def instance-list [stationary-offset oval-offset bottom-circle-offset])

(def vertex-data
  [1.0 1.0 1.0
   -1.0 -1.0 1.0
   -1.0 1.0 -1.0
   1.0 -1.0 -1.0

   -1.0 -1.0 -1.0
   1.0 1.0 -1.0
   1.0 -1.0 1.0
   -1.0 1.0 1.0

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
  [0 1 2
   1 0 3
   2 3 0
   3 2 1

   5 4 6
   4 5 7
   7 6 4
   6 7 5])

