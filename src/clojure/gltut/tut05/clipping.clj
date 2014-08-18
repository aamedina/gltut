(ns gltut.tut05.clipping
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
            [gltut.tut05 :refer :all]
            [gltut.tut05.depth :refer [setup update]])
  (:import (java.nio FloatBuffer)))

(defn draw
  [{:keys [vao the-program offset-uniform]
    :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear-depth 1.0)
  (gl-clear (bit-or GL_COLOR_BUFFER_BIT GL_DEPTH_BUFFER_BIT))
  (with-program the-program
    (with-vertex-array vao
      (gl-uniform3f offset-uniform 0.0 0.0 0.5)
      (gl-draw-elements GL_TRIANGLES (count index-data) GL_UNSIGNED_SHORT 0)
      (gl-uniform3f offset-uniform 0.0 0.0 -1.0)
      (gl-draw-elements-base-vertex GL_TRIANGLES (count index-data)
                                    GL_UNSIGNED_SHORT 0 (/ num-vertices 2)))))

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
