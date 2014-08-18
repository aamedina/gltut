(ns gltut.tut06
  (:require [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix2d :as mat2 :refer [mat2]]
            [lwcgl.math.matrix3d :as mat3 :refer [mat3]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]
             :rename {set-row! set-column! set-column! set-row!}]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]
            [gltut.util :refer :all])
  (:import (java.nio Buffer FloatBuffer)
           (org.lwjgl.util.vector Matrix2f Matrix3f Matrix4f)))

(defprotocol Offsetable
  (calc-offset [this elapsed-time]))

(defprotocol Scalable
  (calc-scale [this offset elapsed-time]))

(defprotocol BufferableData
  (fill-buffer [this ^Buffer buffer]))

(defn construct-offsetable
  [offsetable elapsed-time]
  (let [the-matrix (mat4)
        offset-vec (calc-offset offsetable elapsed-time)]
    (set-column! the-matrix 3 (vec4 (vec3/x offset-vec)
                                    (vec3/y offset-vec)
                                    (vec3/z offset-vec) 1.0))))

(defn construct-scalable
  [scalable elapsed-time]
  (let [offset-vec (calc-offset scalable elapsed-time)
        scale-vec (calc-scale scalable offset-vec elapsed-time)]
    (doto (mat4/scale scale-vec (mat4) (mat4))
      (set-column! 3 (vec4 (vec3/x offset-vec)
                           (vec3/y offset-vec)
                           (vec3/z offset-vec) 1.0)))))

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
(def ^:const mat4-size (/ (* 16 Float/SIZE) Byte/SIZE))

(def stationary-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (vec3 0.0 0.0 -20.0))))

(def oval-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (let [loop-duration 3.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vec3 (* (cos t) 4.0) (* (sin t) 6.0) -20.0)))))

(def bottom-circle-offset
  (reify Offsetable
    (calc-offset [this elapsed-time]
      (let [loop-duration 12.0
            scale (/ (* PI 2) loop-duration)
            curr-time-through-loop (mod elapsed-time loop-duration)
            t (* curr-time-through-loop scale)]
        (vec3 (* (cos t) 5.0) -3.5 (- (* (sin t) 5.0) 20.0))))))

(def null-scale
  (reify
    Scalable
    (calc-scale [this offset elapsed-time]
      (vec3 1.0 1.0 1.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vec3 0.0 0.0 -45.0))))

(def static-uniform-scale
  (reify
    Scalable
    (calc-scale [this offset elapsed-time]
      (vec3 4.0 4.0 4.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vec3 -10.0 -10.0 -45.0))))

(def static-non-uniform-scale
  (reify
    Scalable
    (calc-scale [this offset elapsed-time]
      (vec3 0.5 1.0 10.0))
    Offsetable
    (calc-offset [this elapsed-time]
      (vec3 -10.0 10.0 -45.0))))

(defn calc-lerp-factor
  [elapsed-time loop-duration]
  (let [val (/ (mod elapsed-time loop-duration) loop-duration)]
    (if (> val 0.5)
      (* 2.0 (- 1.0 val))
      (* 2.0 val))))

(def dynamic-uniform-scale
  (reify
    Scalable
    (calc-scale [this offset elapsed-time]
      (let [loop-duration 3.0
            m (mix 1.0 4.0 (calc-lerp-factor elapsed-time loop-duration))]
        (vec3 m m m)))
    Offsetable
    (calc-offset [this elapsed-time]
      (vec3 10.0 10.0 -45.0))))

(def dynamic-non-uniform-scale
  (reify
    Scalable
    (calc-scale [this offset elapsed-time]
      (let [x-loop-duration 3.0
            z-loop-duration 5.0
            mx (mix 1.0 0.5 (calc-lerp-factor elapsed-time x-loop-duration))
            mz (mix 1.0 10.0 (calc-lerp-factor elapsed-time z-loop-duration))]
        (vec3 mx 1.0 mz)))
    Offsetable
    (calc-offset [this elapsed-time]
      (vec3 10.0 -10.0 -45.0))))

(def instance-list [stationary-offset oval-offset bottom-circle-offset])

(def scalables [null-scale static-uniform-scale static-non-uniform-scale
                dynamic-uniform-scale dynamic-non-uniform-scale])

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

