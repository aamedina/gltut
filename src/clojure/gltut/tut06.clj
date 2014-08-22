(ns gltut.tut06
  (:refer-clojure :exclude [vector])
  (:require [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.buffers :as bufs]
            [euclidean.math.matrix :as m :refer [mat2 mat3 mat4]]
            [euclidean.math.vector :as v :refer [vector]]
            [gltut.util :refer :all])
  (:import (java.nio Buffer FloatBuffer)
           (euclidean.math.matrix Matrix2D Matrix3D Matrix4D)))

(defprotocol Offsetable
  (calc-offset [this elapsed-time]))

(defprotocol Scalable
  (calc-scale [this elapsed-time]))

(defprotocol Rotatable
  (calc-rotation [this elapsed-time]))

(defprotocol BufferableData
  (fill-buffer [this ^Buffer buffer]))

(defn assoc-column
  [mat n [x y z w :as v]]
  (-> mat
      (assoc-in [0 n] x)
      (assoc-in [1 n] y)
      (assoc-in [2 n] z)
      (assoc-in [3 n] (or w 1.0))))

(defn construct-offsetable
  [offsetable elapsed-time]
  (assoc-column (mat4) 3 (calc-offset offsetable elapsed-time)))

(defn construct-scalable
  [scalable elapsed-time]
  (-> (m/scale (mat4) (calc-scale scalable elapsed-time))
      (assoc-column 3 (calc-offset scalable elapsed-time))))

(defn construct-rotatable
  [rotatable elapsed-time]
  (-> (calc-rotation rotatable elapsed-time)
      (assoc-column 3 (calc-offset rotatable elapsed-time))))

(defn fill-and-flip-buffer
  [x ^Buffer buffer]
  (.clear buffer)
  (fill-buffer x buffer)
  (.flip buffer)
  buffer)

(defn store
  [m buffer]
  (doto buffer
    (.put (float-array (flatten (m/transpose m))))))

(defn load-buffer
  [buffer]
  (let [dest (float-array (.capacity buffer))]
    (.get buffer dest)
    (case (.capacity buffer)
      4 (m/transpose (apply mat2 dest))
      9 (m/transpose (apply mat3 dest))
      16 (m/transpose (apply mat4 dest))
      dest)))

(extend-protocol BufferableData
  Matrix2D
  (fill-buffer [mat buffer]
    (store mat buffer))
  
  Matrix3D
  (fill-buffer [mat buffer]
    (store mat buffer))
  
  Matrix4D
  (fill-buffer [mat buffer]
    (store mat buffer)))

(defn calculate-frustum-scale
  [fov]
  (float (/ 1.0 (tan (/ (to-radians fov) 2.0)))))

(def ^:const frustum-scale (calculate-frustum-scale 45.0))
(def ^:const num-vertices 8)
(def ^:const mat4-size (/ (* 16 Float/SIZE) Byte/SIZE))

(def stationary-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (vector 0.0 0.0 -20.0))))

(def oval-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (let [loop-duration 3.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vector (* (cos t) 4.0) (* (sin t) 6.0) -20.0)))))

(def bottom-circle-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (let [loop-duration 12.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vector (* (cos t) 5.0) -3.5 (- (* (sin t) 5.0) 20.0))))))

(def null-scale
  (reify
    Scalable
    (calc-scale [this elapsed-time]
      (vector 1.0 1.0 1.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 0.0 0.0 -45.0))))

(def static-uniform-scale
  (reify
    Scalable
    (calc-scale [this elapsed-time]
      (vector 4.0 4.0 4.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector -10.0 -10.0 -45.0))))

(def static-non-uniform-scale
  (reify
    Scalable
    (calc-scale [this elapsed-time]
      (vector 0.5 1.0 10.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector -10.0 10.0 -45.0))))

(defn calc-lerp-factor
  [elapsed-time loop-duration]
  (let [val (/ (mod elapsed-time loop-duration) loop-duration)]
    (* (if (> val 0.5) (- 1.0 val) val) 2.0)))

(def dynamic-uniform-scale
  (reify
    Scalable
    (calc-scale [this elapsed-time]
      (let [loop-duration 3.0
            m (mix 1.0 4.0 (calc-lerp-factor elapsed-time loop-duration))]
        (vector m m m)))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 10.0 10.0 -45.0))))

(def dynamic-non-uniform-scale
  (reify
    Scalable
    (calc-scale [this elapsed-time]
      (let [x-loop-duration 3.0
            z-loop-duration 5.0
            mx (mix 1.0 0.5 (calc-lerp-factor elapsed-time x-loop-duration))
            mz (mix 1.0 10.0 (calc-lerp-factor elapsed-time z-loop-duration))]
        (vector mx 1.0 mz)))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 10.0 -10.0 -45.0))))

(def null-rotation
  (reify
    Rotatable
    (calc-rotation [this elapsed-time]
      (mat4))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 0.0 0.0 -25.0))))

(defn compute-theta
  [elapsed-time loop-duration]
  (* (mod elapsed-time loop-duration) (/ (* PI 2) loop-duration)))

(def rotate-x
  (reify
    Rotatable
    (calc-rotation [this elapsed-time]
      (let [theta (compute-theta elapsed-time 3.0)
            cosine (cos theta)
            sine (sin theta)]
        (-> (mat4)
            (assoc-in [1 1] cosine)
            (assoc-in [1 2] (- sine))
            (assoc-in [1 2] sine)
            (assoc-in [2 2] cosine))))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector -5.0 -5.0 -25.0))))

(def rotate-y
  (reify
    Rotatable
    (calc-rotation [this elapsed-time]
      (let [theta (compute-theta elapsed-time 2.0)
            cosine (cos theta)
            sine (sin theta)]
        (-> (mat4)
            (assoc-in [0 0] cosine)
            (assoc-in [0 2] sine)
            (assoc-in [2 0] (- sine))
            (assoc-in [2 2] cosine))))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector -5.0 5.0 -25.0))))

(def rotate-z
  (reify
    Rotatable
    (calc-rotation [this elapsed-time]
      (let [theta (compute-theta elapsed-time 2.0)
            cosine (cos theta)
            sine (sin theta)]
        (-> (mat4)
            (assoc-in [0 0] cosine)
            (assoc-in [0 1] (- sine))
            (assoc-in [1 0] sine)
            (assoc-in [1 1] cosine))))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 5.0 5.0 -25.0))))

(def rotate-axis
  (reify
    Rotatable
    (calc-rotation [this elapsed-time]
      (let [theta (compute-theta elapsed-time 2.0)
            [x y z :as axis] (v/normalize (vector 1.0 1.0 1.0))
            cosine (cos theta)
            sine (sin theta)
            icosine (- 1 cosine)]
        (-> (mat4)
            (assoc-in [0 0] (+ (* x x) (* (- 1 (* x x)) cosine)))
            (assoc-in [0 1] (- (* x y icosine) (* z sine)))
            (assoc-in [0 2] (+ (* x z icosine) (* y sine)))
            (assoc-in [1 0] (+ (* x y icosine) (* z sine)))
            (assoc-in [1 1] (+ (* y y) (* (- 1 (* y y)) cosine)))
            (assoc-in [1 2] (- (* y z icosine) (* x sine)))
            (assoc-in [2 0] (- (* x z icosine) (* y sine)))
            (assoc-in [2 1] (+ (* y z icosine) (* x sine)))
            (assoc-in [2 2] (+ (* z z) (* (- 1 (* z z)) cosine))))))
    Offsetable
    (calc-offset [this elapsed-time]
      (vector 5.0 -5.0 -25.0))))

(def instance-list [stationary-offset oval-offset bottom-circle-offset])

(def scalables [null-scale static-uniform-scale static-non-uniform-scale
                dynamic-uniform-scale dynamic-non-uniform-scale])

(def rotatables [null-rotation rotate-x rotate-y rotate-z rotate-axis])

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

(def ^:const num-hierarchy-vertices 24)

(def index-data
  [0 1 2
   1 0 3
   2 3 0
   3 2 1

   5 4 6
   4 5 7
   7 6 4
   6 7 5])

(def hierarchy-vertex-data
  [;; Front
   1.0 1.0 1.0
   1.0 -1.0 1.0
   -1.0 -1.0 1.0
   -1.0 1.0 1.0

   ;; Top
   1.0 1.0 1.0
   -1.0 1.0 1.0
   -1.0 1.0 -1.0
   1.0 1.0 -1.0

   ;; Left
   1.0 1.0 1.0
   1.0 1.0 -1.0
   1.0 -1.0 -1.0
   1.0 -1.0 1.0

   ;; Back
   1.0 1.0 -1.0
   -1.0 1.0 -1.0
   -1.0 -1.0 -1.0
   1.0 -1.0 -1.0

   ;; Bottom
   1.0 -1.0 1.0
   1.0 -1.0 -1.0
   -1.0 -1.0 -1.0
   -1.0 -1.0 1.0

   ;; Right
   -1.0 1.0 1.0
   -1.0 -1.0 1.0
   -1.0 -1.0 -1.0
   -1.0 1.0 -1.0
   
   0.0 1.0 0.0 1.0     ;; GREEN_COLOR
   0.0 1.0 0.0 1.0     ;; GREEN_COLOR
   0.0 1.0 0.0 1.0     ;; GREEN_COLOR
   0.0 1.0 0.0 1.0     ;; GREEN_COLOR

   0.0 0.0 1.0 1.0     ;; BLUE_COLOR
   0.0 0.0 1.0 1.0     ;; BLUE_COLOR
   0.0 0.0 1.0 1.0     ;; BLUE_COLOR
   0.0 0.0 1.0 1.0     ;; BLUE_COLOR

   1.0 0.0 0.0 1.0     ;; RED_COLOR
   1.0 0.0 0.0 1.0     ;; RED_COLOR
   1.0 0.0 0.0 1.0     ;; RED_COLOR
   1.0 0.0 0.0 1.0     ;; RED_COLOR

   1.0 1.0 0.0 1.0     ;; YELLOW_COLOR
   1.0 1.0 0.0 1.0     ;; YELLOW_COLOR
   1.0 1.0 0.0 1.0     ;; YELLOW_COLOR
   1.0 1.0 0.0 1.0     ;; YELLOW_COLOR

   0.0 1.0 1.0 1.0     ;; CYAN_COLOR
   0.0 1.0 1.0 1.0     ;; CYAN_COLOR
   0.0 1.0 1.0 1.0     ;; CYAN_COLOR
   0.0 1.0 1.0 1.0     ;; CYAN_COLOR

   1.0 0.0 1.0 1.0     ;; MAGENTA_COLOR
   1.0 0.0 1.0 1.0     ;; MAGENTA_COLOR
   1.0 0.0 1.0 1.0     ;; MAGENTA_COLOR
   1.0 0.0 1.0 1.0      ;; MAGENTA_COLOR
   ])

(def hierarchy-index-data
  [0 1 2
   2 3 0

   4 5 6
   6 7 4

   8 9 10
   10 11 8

   12 13 14
   14 15 12

   16 17 18
   18 19 16

   20 21 22
   22 23 20])
