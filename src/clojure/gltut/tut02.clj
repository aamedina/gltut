(ns gltut.tut02
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
            [gltut.tut01 :as tut01]))

(defn setup
  []
  (print-info *ns*)
  (let [vertex-data [0.0 0.5 0.0 1.0
                     0.5 -0.366 0.0 1.0
                     -0.5 -0.366 0.0 1.0
                     1.0 0.0 0.0 1.0
                     0.0 1.0 0.0 1.0
                     0.0 0.0 1.0 1.0]
        vertex-buffer-object (gen-buffers vertex-data GL_STATIC_DRAW)]
    (-> {:vert "tut02/FragPosition.vert"
         :frag "tut02/FragPosition.frag"}
        (assoc :vertex-buffer-object vertex-buffer-object)
        (init-program)
        (assoc :vao (doto (gl-gen-vertex-arrays)
                      (gl-bind-vertex-array))))))

(defn draw
  [{:keys [vertex-buffer-object the-program] :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (with-program the-program
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (with-vertex-attrib-arrays [0 1]
      (gl-vertex-attrib-pointer 0 4 GL_FLOAT false 0 0)
      (gl-vertex-attrib-pointer 1 4 GL_FLOAT false 0 48)
      (gl-draw-arrays GL_TRIANGLES 0 3))))

(defn tut02
  []
  (when-not (d/created?)
    (future
      (sketch
       :setup (var setup)
       :update (var tut01/update)
       :draw (var draw)
       :dispose (var tut01/dispose)
       :frame-rate 60
       :size [500 500]
       :render-in-background? true
       :title "tut02"))))
