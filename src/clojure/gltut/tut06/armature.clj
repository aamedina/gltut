(ns gltut.tut06.armature
  (:require [lwcgl.core :refer :all]
            [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.opengl.v41 :refer :all]
            [euclidean.math.matrix :as m]
            [euclidean.math.vector :as v]
            [gltut.tut06 :refer :all :exclude [rotate-x rotate-y rotate-z]]
            [gltut.util :refer :all]))

(def ^:const standard-angle-inc 11.25)
(def ^:const small-angle-inc 9.0)

(defn armature
  []
  {:pos-base #math/vector [3.0 -5.0 -40.0]
   :ang-base -45.0
   :pos-base-left #math/vector [2.0 0.0 0.0]
   :pos-base-right #math/vector [-2.0 0.0 0.0]
   :scale-base-z 3.0
   :ang-upper-arm -70.75
   :size-upper-arm 9.0
   :pos-lower-arm #math/vector [0.0 0.0 8.0]
   :ang-lower-arm 60.25
   :len-lower-arm 5.0
   :width-lower-arm 1.5
   :pos-wrist #math/vector [0.0 0.0 5.0]
   :ang-wrist-roll 0.0
   :ang-wrist-pitch 67.5
   :len-wrist 2.0
   :width-wrist 2.0
   :pos-left-finger #math/vector [1.0 0.0 1.0]
   :pos-right-finger #math/vector [-1.0 0.0 1.0]
   :ang-finger-open 45.0
   :len-finger 2.0
   :width-finger 0.5
   :ang-lower-finger 45.0})

(def ^:dynamic *matrix*)
(def ^:dynamic *stack*)

(defmacro push-matrix
  [& body]
  `(binding [*stack* (conj *stack* *matrix*)]
     ~@body
     (set! *matrix* (peek *stack*))))

(defn rotate-x!
  [theta]
  (set! *matrix* (m/rotate-x *matrix* theta)))

(defn rotate-y!
  [theta]
  (set! *matrix* (m/rotate-y *matrix* theta)))

(defn rotate-z!
  [theta]
  (set! *matrix* (m/rotate-z *matrix* theta)))

(defn translate!
  [translation-vec]
  (set! *matrix* (m/translate translation-vec *matrix*)))

(defn scale!
  [scaling-vec]
  (set! *matrix* (m/scale *matrix* scaling-vec)))

(defn fill-and-draw-buffer
  [loc data buffer]
  (gl-uniform-matrix4 loc false (fill-and-flip-buffer *matrix* buffer))
  (gl-draw-elements GL_TRIANGLES (count data) GL_UNSIGNED_SHORT 0))

(defn draw-left-finger
  [{:keys [armature mat4-buffer model] :as state}]
  (push-matrix
    (translate! (:pos-left-finger armature))
    (rotate-y! (:ang-finger-open armature))
    
    (push-matrix
      (translate! (v/vector 0.0 0.0 (/ (:len-finger armature) 2.0)))
      (scale! (v/vector (/ (:width-finger armature) 2.0)
                        (/ (:width-finger armature) 2.0)
                        (/ (:len-finger armature) 2.0)))
      (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

    (push-matrix
      (translate! (v/vector 0.0 0.0 (:len-finger armature)))
      (rotate-y! (- (:ang-lower-finger armature)))

      (push-matrix
        (translate! (v/vector 0.0 0.0 (/ (:len-finger armature) 2.0)))
        (scale! (v/vector (/ (:width-finger armature) 2.0)
                          (/ (:width-finger armature) 2.0)
                          (/ (:len-finger armature) 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer)))))

(defn draw-right-finger
  [{:keys [armature mat4-buffer model] :as state}]
  (push-matrix
    (translate! (:pos-right-finger armature))
    (rotate-y! (- (:ang-finger-open armature)))
    
    (push-matrix
      (translate! (v/vector 0.0 0.0 (/ (:len-finger armature) 2.0)))
      (scale! (v/vector (/ (:width-finger armature) 2.0)
                        (/ (:width-finger armature) 2.0)
                        (/ (:len-finger armature) 2.0)))
      (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

    (push-matrix
      (translate! (v/vector 0.0 0.0 (:len-finger armature)))
      (rotate-y! (:ang-lower-finger armature))

      (push-matrix
        (translate! (v/vector 0.0 0.0 (/ (:len-finger armature) 2.0)))
        (scale! (v/vector (/ (:width-finger armature) 2.0)
                          (/ (:width-finger armature) 2.0)
                          (/ (:len-finger armature) 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer)))))

(defn draw-fingers
  [{:keys [armature mat4-buffer model] :as state}]
  (draw-left-finger state)
  (draw-right-finger state))

(defn draw-wrist
  [{:keys [armature mat4-buffer model] :as state}]
  (push-matrix
    (translate! (:pos-wrist armature))
    (rotate-z! (:ang-wrist-roll armature))
    (rotate-x! (:ang-wrist-pitch armature))
    
    (push-matrix
      (scale! (v/vector (/ (:width-wrist armature) 2.0)
                        (/ (:width-wrist armature) 2.0)
                        (/ (:len-wrist armature) 2.0)))
      (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

    (draw-fingers state)))

(defn draw-lower-arm
  [{:keys [armature model mat4-buffer] :as state}]
  (push-matrix
    (translate! (:pos-lower-arm armature))
    (rotate-x! (:ang-lower-arm armature))
    
    (push-matrix
      (translate! (v/vector 0.0 0.0 (/ (:len-lower-arm armature) 2.0)))
      (scale! (v/vector (/ (:width-lower-arm armature) 2.0)
                        (/ (:width-lower-arm armature) 2.0)
                        (/ (:len-lower-arm armature) 2.0)))
      (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))
    
    (draw-wrist state)))

(defn draw-upper-arm
  [{:keys [armature model mat4-buffer] :as state}]
  (push-matrix
    (rotate-x! (:ang-upper-arm armature))
    
    (push-matrix
      (translate! (v/vector 0.0 0.0 (dec (/ (:size-upper-arm armature) 2.0))))
      (scale! (v/vector 1.0 1.0 (/ (:size-upper-arm armature) 2.0)))
      (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

    (draw-lower-arm state)))

(defn draw
  [{:keys [armature the-program model vao mat4-buffer] :as state}]
  (binding [*matrix* (m/mat4)
            *stack* []]
    (with-program the-program
      (with-vertex-array vao
        
        (translate! (:pos-base armature))
        (rotate-y! (:ang-base armature))
        
        (push-matrix
          (translate! (:pos-base-left armature))
          (scale! (v/vector 1.0 1.0 (:scale-base-z armature)))
          (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

        (push-matrix
          (translate! (:pos-base-right armature))
          (scale! (v/vector 1.0 1.0 (:scale-base-z armature)))
          (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))
        
        (draw-upper-arm state)))))

(defn maybe-increment
  [armature k const increment?]
  (let [last-frame-duration (/ (* *last-frame-duration* 5) 1000.0)]
    (update-in armature [k]
               #(+ % (* (if increment? const (- const)) last-frame-duration)))))

(defn adjust-base
  [armature increment?]
  (-> armature
      (maybe-increment :ang-base standard-angle-inc increment?)
      (update-in [:ang-base] mod 360.0)))

(defn adjust-upper-arm
  [armature increment?]
  (-> armature
      (maybe-increment :ang-upper-arm standard-angle-inc increment?)
      (update-in [:ang-upper-arm] clamp -90.0 0.0)))

(defn adjust-lower-arm
  [armature increment?]
  (-> armature
      (maybe-increment :ang-lower-arm standard-angle-inc increment?)
      (update-in [:ang-lower-arm] clamp 0.0 146.25)))

(defn adjust-wrist-pitch
  [armature increment?]
  (-> armature
      (maybe-increment :ang-wrist-pitch standard-angle-inc increment?)
      (update-in [:ang-wrist-pitch] clamp 0.0 90.0)))

(defn adjust-wrist-roll
  [armature increment?]
  (-> armature
      (maybe-increment :ang-wrist-roll standard-angle-inc increment?)
      (update-in [:ang-wrist-roll] mod 360.0)))

(defn adjust-finger-open
  [armature increment?]
  (-> armature
      (maybe-increment :ang-finger-open small-angle-inc increment?)
      (update-in [:ang-finger-open] clamp 9.0 90.0)))
