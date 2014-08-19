(ns gltut.core
  (:gen-class)
  (:require [gltut.tut01 :refer [tut01]]
            [gltut.tut02 :refer [tut02]]
            [gltut.tut03 :refer [tut03]]
            [gltut.tut04 :refer [tut04]]
            [gltut.tut05.overlap :as overlap]
            [gltut.tut06.translation :as trans]
            [gltut.tut06.hierarchy :as h]))

(defn -main
  [& args]
  (h/tut06))
