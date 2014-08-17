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
  (:import java.nio.FloatBuffer))

(defn init-vertex-buffer
  [{:keys [vertex-positions] :as state}]
  (assoc state
    :vertex-positions-buffer (buffer-of :float vertex-positions)))

(defn init-position-buffer-object
  [{:keys [vertex-positions-buffer] :as state}]
  (let [pbo (gl-gen-buffers)]
    (gl-bind-buffer GL_ARRAY_BUFFER pbo)
    (gl-buffer-data GL_ARRAY_BUFFER vertex-positions-buffer GL_STATIC_DRAW)
    (gl-bind-buffer GL_ARRAY_BUFFER 0)
    (assoc state
      :position-buffer-object pbo)))

(defn setup
  []
  (print-info *ns*)
  (-> {:vertex-positions (float-array [0.75 0.75 0.0 1.0
                                       0.75 -0.75 0.0 1.0
                                       -0.75 -0.75 0.0 1.0])
       :vert "tut01.vert"
       :frag "tut01.frag"}
      (init-vertex-buffer)
      (init-position-buffer-object)
      (init-program)
      (assoc :vao (doto (gl-gen-vertex-arrays)
                    (gl-bind-vertex-array)))))

(defn update
  [state]
  (cond-> state
    (kb/key-down? kb/KEY_ESCAPE) (assoc :finished? true)))

(defn draw
  [{:keys [the-program position-buffer-object] :as state}]
  (gl-clear-color 0 (/ 140 255.0) (/ 200 255.0) 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (gl-use-program the-program)
  (gl-bind-buffer GL_ARRAY_BUFFER position-buffer-object)
  (gl-enable-vertex-attrib-array 0)
  (gl-vertex-attrib-pointer 0 4 GL_FLOAT false 0 0)
  (gl-draw-arrays GL_TRIANGLES 0 3)
  (gl-disable-vertex-attrib-array 0)
  (gl-use-program 0))

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
       :size [720 150]
       :render-in-background? true
       :title "tut01"))))

