(ns gltut.tut06.armature
  (:require [lwcgl.core :refer :all]
            [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]
            [lwcgl.math.matrix3d :as mat3 :refer [mat3]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]]
            [lwcgl.opengl.v41 :refer :all]
            [gltut.tut06 :refer :all :exclude [rotate-x rotate-y rotate-z]]
            [gltut.util :refer :all]))

(def ^:const standard-angle-inc 11.25)
(def ^:const small-angle-inc 9.0)

(defn armature
  []
  {:pos-base (vec3 3.0 -5.0 -40.0)
   :ang-base -45.0
   :pos-base-left (vec3 2.0 0.0 0.0)
   :pos-base-right (vec3 -2.0 0.0 0.0)
   :scale-base-z 3.0
   :ang-upper-arm -70.75
   :size-upper-arm 9.0
   :pos-lower-arm (vec3 0.0 0.0 8.0)
   :ang-lower-arm 60.25
   :len-lower-arm 5.0
   :width-lower-arm 1.5
   :pos-wrist (vec3 0.0 0.0 5.0)
   :ang-wrist-roll 0.0
   :ang-wrist-pitch 67.5
   :len-wrist 2.0
   :width-wrist 2.0
   :pos-left-finger (vec3 1.0 0.0 1.0)
   :pos-right-finger (vec3 -1.0 0.0 1.0)
   :ang-finger-open 45.0
   :len-finger 2.0
   :width-finger 0.5
   :ang-lower-finger 45.0})

(def ^:dynamic *matrix*)
(def ^:dynamic *stack*)

(defmacro push-matrix
  [& body]
  `(binding [*stack* (conj *stack* (mat4 *matrix*))]
     ~@body
     (set! *matrix* (peek *stack*))))

(defn rotate-x
  [theta]
  (let [theta (to-radians theta)
        cosine (cos theta)
        sine (sin theta)]
    (doto (mat4)
      (mat4/set-matrix! 1 1 cosine)
      (mat4/set-matrix! 2 1 (- sine))
      (mat4/set-matrix! 1 2 sine)
      (mat4/set-matrix! 2 2 cosine))))

(defn rotate-y
  [theta]
  (let [theta (to-radians theta)
        cosine (cos theta)
        sine (sin theta)]
    (doto (mat4)
      (mat4/set-matrix! 0 0 cosine)
      (mat4/set-matrix! 2 0 sine)
      (mat4/set-matrix! 0 2 (- sine))
      (mat4/set-matrix! 2 2 cosine))))

(defn rotate-z
  [theta]
  (let [theta (to-radians theta)
        cosine (cos theta)
        sine (sin theta)]
    (doto (mat4)
      (mat4/set-matrix! 0 0 cosine)
      (mat4/set-matrix! 1 0 (- sine))
      (mat4/set-matrix! 0 1 sine)
      (mat4/set-matrix! 1 1 cosine))))

(defn rotate-current-x
  [theta]
  (mat4/mul *matrix* (rotate-x theta) *matrix*))

(defn rotate-current-y
  [theta]
  (mat4/mul *matrix* (rotate-y theta) *matrix*))

(defn rotate-current-z
  [theta]
  (mat4/mul *matrix* (rotate-z theta) *matrix*))

(defn translate
  [offset]
  (let [offset-vec4 (vec4 (.-x offset) (.-y offset) (.-z offset) 1.0)
        translation-mat (mat4/set-row! (mat4) 3 offset-vec4)]
    (mat4/mul *matrix* translation-mat *matrix*)))

(defn scale
  [scale-vec]
  (let [scale-mat (doto (mat4)
                    (mat4/set-matrix! 0 0 (.-x scale-vec))
                    (mat4/set-matrix! 1 1 (.-y scale-vec))
                    (mat4/set-matrix! 2 2 (.-z scale-vec)))]
    (mat4/mul *matrix* scale-mat *matrix*)))

(defn fill-and-draw-buffer
  [loc data buffer]
  (gl-uniform-matrix4 loc false (fill-and-flip-buffer (peek *stack*) buffer))
  (gl-draw-elements GL_TRIANGLES (count data) GL_UNSIGNED_SHORT 0))

(defn draw-fingers
  [{:keys [armature mat4-buffer model] :as state}]
  (let [{:keys [pos-left-finger ang-finger-open len-finger width-finger
                ang-lower-finger pos-right-finger pos-wrist]}
        armature]
    (push-matrix
      (translate pos-left-finger)
      (rotate-current-y ang-finger-open)
      
      (push-matrix
        (translate (vec3 0.0 0.0 (/ len-finger 2.0)))
        (scale (vec3 (/ width-finger 2.0)
                     (/ width-finger 2.0)
                     (/ len-finger 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

      (push-matrix
        (translate (vec3 0.0 0.0 len-finger))
        (rotate-current-y (- ang-lower-finger))

        (push-matrix
          (translate (vec3 0.0 0.0 (/ len-finger 2.0)))
          (scale (vec3 (/ width-finger 2.0)
                       (/ width-finger 2.0)
                       (/ len-finger 2.0)))
          (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))))

    (push-matrix
      (translate pos-right-finger)
      (rotate-current-y (- ang-finger-open))
      
      (push-matrix
        (translate (vec3 0.0 0.0 (/ len-finger 2.0)))
        (scale (vec3 (/ width-finger 2.0)
                     (/ width-finger 2.0)
                     (/ len-finger 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

      (push-matrix
        (translate (vec3 0.0 0.0 len-finger))
        (rotate-current-y ang-lower-finger)

        (push-matrix
          (translate (vec3 0.0 0.0 (/ len-finger 2.0)))
          (scale (vec3 (/ width-finger 2.0)
                       (/ width-finger 2.0)
                       (/ len-finger 2.0)))
          (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))))))

(defn draw-wrist
  [{:keys [armature mat4-buffer model] :as state}]
  (let [{:keys [width-wrist ang-wrist-roll ang-wrist-pitch pos-wrist len-wrist]}
        armature]
    (push-matrix
      (translate pos-wrist)
      (rotate-current-z ang-wrist-roll)
      (rotate-current-x ang-wrist-pitch)
      
      (push-matrix
        (scale (vec3 (/ width-wrist 2.0)
                     (/ width-wrist 2.0)
                     (/ len-wrist 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

      (draw-fingers state))))

(defn draw-lower-arm
  [{:keys [armature model mat4-buffer] :as state}]
  (let [{:keys [pos-lower-arm ang-lower-arm len-lower-arm width-lower-arm]}
        armature]
    (push-matrix
      (translate pos-lower-arm)
      (rotate-current-x ang-lower-arm)
      
      (push-matrix
        (translate (vec3 0.0 0.0 (/ len-lower-arm 2.0)))
        (scale (vec3 (/ width-lower-arm 2.0)
                     (/ width-lower-arm 2.0)
                     (/ len-lower-arm 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))
      
      (draw-wrist state))))

(defn draw-upper-arm
  [{:keys [armature model mat4-buffer] :as state}]
  (let [{:keys [ang-upper-arm size-upper-arm]}
        armature]
    (push-matrix
      (rotate-current-x ang-upper-arm)
      
      (push-matrix
        (translate (vec3 0.0 0.0 (dec (/ size-upper-arm 2.0))))
        (scale (vec3 1.0 1.0 (/ size-upper-arm 2.0)))
        (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

      (draw-lower-arm state))))

(defn draw
  [{:keys [armature the-program model vao mat4-buffer] :as state}]
  (let [{:keys [pos-base ang-base pos-base-right pos-base-left scale-base-z]}
        armature]
    (binding [*matrix* (mat4)
              *stack* []]
      (with-program the-program
        (with-vertex-array vao
          (translate pos-base)
          (rotate-current-y ang-base)
          
          (push-matrix
            (translate pos-base-left)
            (scale (vec3 1.0 1.0 scale-base-z))
            (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

          (push-matrix
            (translate pos-base-right)
            (scale (vec3 1.0 1.0 scale-base-z))
            (fill-and-draw-buffer model hierarchy-index-data mat4-buffer))

          (draw-upper-arm state))))))

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
