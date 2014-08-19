(ns gltut.math.mat2
  (:refer-clojure :exclude [identity]))

(def identity
  [[1.0 0.0]
   [0.0 1.0]])

(defn add
  [[[x00 x01] [x10 x11]] [[y00 y01] [y10 y11]]]
  [[(+ x00 y00) (+ x01 y01)]
   [(+ x10 y10) (+ x11 y11)]])
