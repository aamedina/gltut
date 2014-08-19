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

(declare rotate-x rotate-y rotate-z)

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

(defn draw-fingers
  [{:keys [armature mat4buffer model] :as state}]
  (let [{:keys [pos-left-finger ang-finger-open len-finger width-finger
                ang-lower-finger pos-right-finger pos-wrist]}
        armature]
    (push-matrix
      (.translate *matrix* pos-wrist)
      (.mul *matrix* (rotate-y ang-finger-open) *matrix*)
      
      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 (/ len-finger 2.0)))
        (.scale *matrix* (vec3 (/ width-finger 2.0)
                               (/ width-finger 2.0)
                               (/ len-finger 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0)))

      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 len-finger))
        (.mul *matrix* (rotate-y (- ang-lower-finger)) *matrix*)
        (.scale *matrix* (vec3 (/ width-finger 2.0)
                               (/ width-finger 2.0)
                               (/ len-finger 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0))))

    (push-matrix
      (.translate *matrix* pos-right-finger)
      (.mul *matrix* (rotate-y (- ang-finger-open)) *matrix*)
      
      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 (/ len-finger 2.0)))
        (.scale *matrix* (vec3 (/ width-finger 2.0)
                               (/ width-finger 2.0)
                               (/ len-finger 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0)))

      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 len-finger))
        (.mul *matrix* (rotate-y ang-lower-finger) *matrix*)

        (push-matrix
          (.translate *matrix* (vec3 0.0 0.0 (/ len-finger 2.0)))
          (.scale *matrix* (vec3 (/ width-finger 2.0)
                                 (/ width-finger 2.0)
                                 (/ len-finger 2.0)))
          (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
            (gl-uniform-matrix4 model false buf)
            (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                              GL_UNSIGNED_SHORT 0)))))))

(defn draw-wrist
  [{:keys [armature mat4buffer model] :as state}]
  (let [{:keys [width-wrist ang-wrist-roll ang-wrist-pitch pos-wrist len-wrist]}
        armature]
    (push-matrix
      (.translate *matrix* pos-wrist)
      (.mul *matrix* (rotate-z ang-wrist-roll) *matrix*)
      (.mul *matrix* (rotate-x ang-wrist-pitch) *matrix*)
      
      (push-matrix
        (.scale *matrix* (vec3 (/ width-wrist 2.0)
                               (/ width-wrist 2.0)
                               (/ len-wrist 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0)))

      (draw-fingers state))))

(defn draw-lower-arm
  [{:keys [armature model mat4buffer] :as state}]
  (let [{:keys [pos-lower-arm ang-lower-arm len-lower-arm width-lower-arm]}
        armature]
    (push-matrix
      (.translate *matrix* pos-lower-arm)
      (.mul *matrix* (rotate-x ang-lower-arm))
      
      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 (/ len-lower-arm 2.0)))
        (.scale *matrix* (vec3 (/ width-lower-arm 2.0)
                               (/ width-lower-arm 2.0)
                               (/ len-lower-arm 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0)))

      (draw-wrist state))))

(defn draw-upper-arm
  [{:keys [armature model mat4buffer] :as state}]
  (let [{:keys [ang-upper-arm size-upper-arm]}
        armature]
    (push-matrix
      (.mul *matrix* (rotate-x ang-upper-arm))
      
      (push-matrix
        (.translate *matrix* (vec3 0.0 0.0 (dec (/ size-upper-arm 2.0))))
        (.scale *matrix* (vec3 0.0 0.0 (/ size-upper-arm 2.0)))
        (let [buf (fill-and-flip-buffer (peek *stack*) mat4buffer)]
          (gl-uniform-matrix4 model false buf)
          (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                            GL_UNSIGNED_SHORT 0)))

      (draw-lower-arm state))))

(defn draw
  [{:keys [armature the-program model vao mat4-buffer] :as state}]
  (let [{:keys [pos-base ang-base pos-base-right pos-base-left scale-base-z]}
        armature]
    (binding [*matrix* (mat4)
              *stack* []]
      (with-program the-program
        (with-vertex-array vao          
          (.translate *matrix* pos-base)
          (.mul *matrix* (rotate-y ang-base) *matrix*)
          
          (push-matrix
            (println *matrix*)
            (.translate *matrix* pos-base-left)
            (.scale *matrix* (vec3 1.0 1.0 scale-base-z))
            (let [buf (fill-and-flip-buffer (peek *stack*) mat4-buffer)]
              (gl-uniform-matrix4 model false buf))
            (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                              GL_UNSIGNED_SHORT 0))

          (push-matrix
            (println *matrix*)
            (.translate *matrix* pos-base-right)
            (.scale *matrix* (vec3 1.0 1.0 scale-base-z))
            (let [buf (fill-and-flip-buffer (peek *stack*) mat4-buffer)]
              (gl-uniform-matrix4 model false buf))
            (gl-draw-elements GL_TRIANGLES (count hierarchy-index-data)
                              GL_UNSIGNED_SHORT 0))

          (draw-upper-arm state))))))

(defn maybe-increment
  [armature k const increment?]
  (let [last-frame-duration (/ (* *last-frame-duration* 5) 1000.0)]
    (update-in armature [k]
               (fn [x]
                 (if increment?
                   (+ x (* const last-frame-duration))
                   (+ x (* (- const) last-frame-duration)))))))

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
      (update-in [:ang-finger-open] clamp 9.0 9.0)))

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
