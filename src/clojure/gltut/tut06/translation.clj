(ns gltut.tut06.translation
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
            [lwcgl.math.matrix2d :as mat2 :refer [mat2]]
            [lwcgl.math.matrix3d :as mat3 :refer [mat3]]
            [lwcgl.math.matrix4d :as mat4 :refer [mat4]]
            [lwcgl.math.vector2d :as vec2 :refer [vec2]]
            [lwcgl.math.vector3d :as vec3 :refer [vec3]]
            [lwcgl.math.vector4d :as vec4 :refer [vec4]]
            [lwcgl.math.quaternion :as q]
            
            [clojure.java.io :as io]

            [gltut.util :refer :all]
            [gltut.tut01 :as tut01]
            [gltut.tut06 :refer :all])
  (:import (java.nio FloatBuffer)))

(defn setup
  []
  (print-info *ns*)
  (let [shaders {:vert "tut06/PosColorLocalTransform.vert"
                 :frag "tut06/ColorPassthrough.frag"}
        {:keys [the-program] :as state} (init-program shaders)
        state
        (assoc state
          :start-time (System/nanoTime)
          :vertex-data vertex-data
          :vertex-buffer-object (gen-buffers :float vertex-data GL_STATIC_DRAW)
          :index-buffer-object (gen-buffers :short index-data GL_STATIC_DRAW)
          :model (gl-get-uniform-location the-program "modelToCameraMatrix")
          :clip (gl-get-uniform-location the-program "cameraToClipMatrix"))
        z-near 1.0
        z-far 45.0
        camera-to-clip-matrix (doto (mat4/set-zero! (mat4))
                                (mat4/set-matrix! 0 0 frustum-scale)
                                (mat4/set-matrix! 1 1 frustum-scale)
                                (mat4/set-matrix! 2 2 (/ (+ z-far z-near)
                                                         (- z-near z-far)))
                                (mat4/set-matrix! 3 2 (/ (* 2 z-far z-near)
                                                         (- z-near z-far)))
                                (mat4/set-matrix! 2 3 -1.0))
        mat4-buffer (buffers/create-float-buffer (/ (* 16 Float/SIZE)
                                                    Byte/SIZE))
        flipped (fill-and-flip-buffer camera-to-clip-matrix mat4-buffer)
        color-data-offset (* 12 num-vertices)]
    (with-program the-program
      (gl-uniform-matrix4 (:clip state) false flipped))
    (let [vao (gl-gen-vertex-arrays)]
      (with-vertex-array vao
        (gl-bind-buffer GL_ARRAY_BUFFER (:vertex-buffer-object state))
        (gl-enable-vertex-attrib-array 0)
        (gl-enable-vertex-attrib-array 1)
        (gl-vertex-attrib-pointer 0 3 GL_FLOAT false 0 0)
        (gl-vertex-attrib-pointer 1 4 GL_FLOAT false 0 color-data-offset)
        (gl-bind-buffer GL_ELEMENT_ARRAY_BUFFER (:index-buffer-object state)))

      (gl-enable GL_CULL_FACE)
      (gl-cull-face GL_BACK)
      (gl-front-face GL_CW)

      (gl-enable GL_DEPTH_TEST)
      (gl-depth-mask true)
      (gl-depth-func GL_LEQUAL)
      (gl-depth-range 0.0 1.0)   

      (assoc state
        :mat4-buffer mat4-buffer
        :camera-to-clip-matrix camera-to-clip-matrix
        :vao vao))))

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
  [{:keys [vao the-program offset-uniform elapsed-time model mat4-buffer]
    :as state}]
  (gl-clear-color 0.0 0.0 0.0 0.0)
  (gl-clear-depth 1.0)
  (gl-clear (bit-or GL_COLOR_BUFFER_BIT GL_DEPTH_BUFFER_BIT))
  (with-program the-program
    (with-vertex-array vao
      (let [elapsed-time (/ elapsed-time 1000.0)]
        (doseq [instance instance-list]
          (let [transform-matrix (construct-offsetable instance elapsed-time)
                flipped (fill-and-flip-buffer transform-matrix mat4-buffer)]
            (gl-uniform-matrix4 model false flipped)
            (gl-draw-elements GL_TRIANGLES (count index-data)
                              GL_UNSIGNED_SHORT 0)))))))

(defn tut06
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
       :title "tut06"))))
