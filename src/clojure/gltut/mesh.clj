(ns gltut.mesh
  (:require [lwcgl.opengl.v41 :refer :all]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn mesh
  [path]
  (edn/read-string (slurp (io/resource path))))
