(ns gltut.protocols
  (:require [euclidean.math.matrix :as m])
  (:import (java.nio Buffer FloatBuffer)
           (euclidean.math.matrix Matrix2D Matrix3D Matrix4D)))

(defprotocol BufferableData
  (fill-buffer [this ^Buffer buffer]))

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
