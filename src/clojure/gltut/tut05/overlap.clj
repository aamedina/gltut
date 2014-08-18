(ns gltut.tut05.overlap
  (:require [lwcgl.core :refer [sketch]]
            [lwcgl.buffers :as buffers]
            [lwcgl.openal :as al]
            [lwcgl.sys :as sys]
            [lwcgl.util.glu :refer :all]
            
            [lwcgl.opengl :refer :all]
            [lwcgl.opengl.v41 :refer :all]
            [lwcgl.opengl.display :as d :refer [width height]]            
            
            [lwcgl.input.keyboard :as kb]
            [lwcgl.input.mouse :as mouse]
            
            [lwcgl.math :refer :all :exclude [min max]]
            [lwcgl.math.matrix2d :as mat2]
            [lwcgl.math.matrix3d :as mat3]
            [lwcgl.math.matrix4d :as mat4]
            [lwcgl.math.vector2d :as vec2]
            [lwcgl.math.vector3d :as vec3]
            [lwcgl.math.vector4d :as vec4]
            [lwcgl.math.quaternion :as q]
            
            [clojure.java.io :as io]

            [gltut.util :refer :all]
            [gltut.tut01 :as tut01]
            [gltut.tut05 :refer :all])
  (:import (java.nio FloatBuffer)))

(defn init-vertex-array-objects
  [{:keys [vertex-buffer-object index-buffer-object] :as state}]
  (let [vao1 (gl-gen-vertex-arrays)]
    (gl-bind-vertex-array vao1)
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (gl-enable-vertex-attrib-array 0)
    (gl-enable-vertex-attrib-array 1)
    (gl-vertex-attrib-pointer 0 3 GL_FLOAT false 0 0)
    (gl-vertex-attrib-pointer 1 4 GL_FLOAT false 0 color-data-offset)
    (gl-bind-buffer GL_ELEMENT_ARRAY_BUFFER index-buffer-object)
    (gl-bind-vertex-array 0)
    (let [vao2 (gl-gen-vertex-arrays)
          pos-data-offset (* 12 (/ num-vertices 2))
          color-data-offset (+ color-data-offset (* 16 (/ num-vertices 2)))]
      (gl-bind-vertex-array vao2)
      (gl-enable-vertex-attrib-array 0)
      (gl-enable-vertex-attrib-array 1)
      (gl-vertex-attrib-pointer 0 3 GL_FLOAT false 0 pos-data-offset)
      (gl-vertex-attrib-pointer 1 4 GL_FLOAT false 0 color-data-offset)
      (gl-bind-buffer GL_ELEMENT_ARRAY_BUFFER index-buffer-object)
      (gl-bind-vertex-array 0)
      (assoc state
        :vao1 vao1
        :vao2 vao2))))

(defn setup
  []
  (print-info *ns*)
  (let [{:keys [the-program] :as state} (init-program
                                         {:vert "tut05/Standard.vert"
                                          :frag "tut05/Standard.frag"})
        state
        (assoc state
          :start-time (System/nanoTime)
          :vertex-data vertex-data
          :vertex-buffer-object (gen-buffers :float vertex-data GL_STATIC_DRAW)
          :index-buffer-object (gen-buffers :short index-data GL_STATIC_DRAW)
          :offset-uniform (gl-get-uniform-location the-program "offset")
          :p-matrix (gl-get-uniform-location the-program "perspectiveMatrix"))
        z-near 1.0
        z-far 3.0
        the-matrix [frustum-scale 0 0 0
                    0 frustum-scale 0 0
                    0 0 (/ (+ z-far z-near) (- z-near z-far)) -1.0
                    0 0 (/ (* z-far z-near 2) (- z-near z-far)) 0]
        matrix-buffer (buffer-of :float (float-array the-matrix))]
    (with-program the-program
      (gl-uniform-matrix4 (:p-matrix state) false matrix-buffer))
    (gl-enable GL_CULL_FACE)
    (gl-cull-face GL_BACK)
    (gl-front-face GL_CW)
    (init-vertex-array-objects state)))

(defn update
  [{:keys [start-time last-frame-timestamp the-program] :as state}]
  (let [elapsed-time (/ (- (System/nanoTime) start-time) (float 1000000.0))
        now (System/nanoTime)
        last-frame-duration (/ (- now (or last-frame-timestamp 0))
                               (float 1000000.0))]
    (assoc state
      :elapsed-time elapsed-time
      :last-frame-duration last-frame-duration
      :last-frame-timestamp now
      :finished? (kb/key-down? kb/KEY_ESCAPE))))

(defn draw
  [{:keys [vao1 vao2 the-program offset-uniform]
    :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (with-program the-program
    (with-vertex-array vao1
      (gl-uniform3f offset-uniform 0.0 0.0 0.0)
      (gl-draw-elements GL_TRIANGLES (count index-data) GL_UNSIGNED_SHORT 0)
      (with-vertex-array vao2
        (gl-uniform3f offset-uniform 0.0 0.0 -1.0)
        (gl-draw-elements GL_TRIANGLES (count index-data)
                          GL_UNSIGNED_SHORT 0)))))

(defn tut05
  []
  (when-not (d/created?)
    (future
      (sketch
       :setup (var setup)
       :update (var update)
       :draw (var draw)
       :dispose (var tut01/dispose)
       :frame-rate 60
       :size [500 500]
       :render-in-background? true
       :title "tut05"))))
