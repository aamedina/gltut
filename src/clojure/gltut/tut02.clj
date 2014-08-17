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
  (-> {:vertex-positions (float-array [0.75 0.75 0.0 1.0
                                       0.75 -0.75 0.0 1.0
                                       -0.75 -0.75 0.0 1.0])
       :vert "tut02/FragPosition.vert"
       :frag "tut02/FragPosition.frag"}
      (init-vertex-buffer)
      (init-position-buffer-object)
      (init-program)
      (assoc :vao (doto (gl-gen-vertex-arrays)
                    (gl-bind-vertex-array)))))

(defn tut02
  []
  (when-not (d/created?)
    (future
      (sketch
       :setup (var setup)
       :update (var tut01/update)
       :draw (var tut01/draw)
       :dispose (var tut01/dispose)
       :frame-rate 60
       :size [720 450]
       :render-in-background? true
       :title "tut02"))))

