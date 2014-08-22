(ns gltut.opengl
  (:require [lwcgl.opengl.v41 :refer :all]
            [euclidean.math.matrix :refer [mat4]]
            [gltut.protocols :refer :all]))

(def ^:dynamic *stack*)
(def ^:dynamic *matrix*)

(defmacro with-matrix-stack
  [init-matrix & body]
  `(binding [*stack* []
             *matrix* ~init-matrix]
     ~@body))

(defmacro push-matrix
  [& body]
  `(binding [*stack* (conj *stack* *matrix*)]
     ~@body
     (set! *matrix* (peek *stack*))))

(defmacro with-program
  [program buffer & body]
  `(let [buf# ~buffer
         prog# ~program
         ret# (do (gl-use-program (:program prog#))
                  (gl-uniform-matrix4 (:location prog#) false
                                      (fill-and-flip-buffer *matrix* buf#))
                  ~@body)]
     (gl-use-program 0)
     ret#))
