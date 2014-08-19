(ns gltut.math.mat3
  (:refer-clojure :exclude [identity]))

(def identity
  [[1.0 0.0 0.0]
   [0.0 1.0 0.0]
   [0.0 0.0 1.0]])

(defn determinant
  [[[t00 t01 t02]
    [t10 t11 t12]
    [t20 t21 t22]]]
  (+ (* t00 (- (* t11 t22) (* t12 t21)))
     (* t01 (- (* t12 t20) (* t10 t22)))
     (* t02 (- (* t10 t21) (* t11 t20)))))
