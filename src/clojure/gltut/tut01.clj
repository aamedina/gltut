(ns gltut.tut01
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

            [gltut.util :refer :all])
  (:import (java.nio FloatBuffer)))

(defn setup
  []
  (print-info *ns*)
  (-> {:vert "tut01.vert"
       :frag "tut01.frag"
       :glsl-watcher (register file-watcher (path "src/glsl") :modify)}
      (init-vertex-buffer (float-array [0.75 0.75 0.0 1.0
                                        0.75 -0.75 0.0 1.0
                                        -0.75 -0.75 0.0 1.0]))
      (init-position-buffer-object)
      (init-program)
      (assoc :vao (doto (gl-gen-vertex-arrays)
                    (gl-bind-vertex-array)))))

(defn update-shader
  [{:keys [glsl-watcher] :as state}]
  (init-program state))

(defn update
  [state]
  (cond-> (update-shader state)
    (kb/key-down? kb/KEY_ESCAPE) (assoc :finished? true)))

(defn draw
  [{:keys [the-program position-buffer-object] :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (with-program the-program
    (gl-bind-buffer GL_ARRAY_BUFFER position-buffer-object)
    (with-vertex-attrib-arrays [0]
      (gl-vertex-attrib-pointer 0 4 GL_FLOAT false 0 0)
      (gl-draw-arrays GL_TRIANGLES 0 3))))

(defn dispose
  [sketch]
  (al/destroy)
  (d/destroy))

(defn tut01
  []
  (when-not (d/created?)
    (future
      (sketch
       :setup (var setup)
       :update (var update)
       :draw (var draw)
       :dispose (var dispose)
       :frame-rate 60
       :size [500 500]
       :render-in-background? true
       :title "tut01"))))

