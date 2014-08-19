(ns gltut.tut06.hierarchy
  (:require [lwcgl.core :refer :all]
            [lwcgl.buffers :as buffers]
            [lwcgl.openal :as al]
            [lwcgl.sys :as sys]
            [lwcgl.macros :refer [do-when]]
            [lwcgl.util.glu :refer :all]
            
            [lwcgl.opengl :as gl]
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
            [gltut.tut06 :refer :all]
            [gltut.tut06.armature :as arm])
  (:import (java.nio FloatBuffer)))

(defn initialize-vao
  [state]
  (let [color-data-offset (* 12 num-hierarchy-vertices)
        vertex-buffer-object (gen-buffers :float hierarchy-vertex-data
                                          GL_STATIC_DRAW)
        index-buffer-object (gen-buffers :short hierarchy-index-data
                                         GL_STATIC_DRAW)
        vao (gl-gen-vertex-arrays)]
    
    (with-vertex-array vao
      (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
      (gl-enable-vertex-attrib-array (:position-attrib state))
      (gl-enable-vertex-attrib-array (:color-attrib state))
      (gl-vertex-attrib-pointer (:position-attrib state) 3 GL_FLOAT false 0 0)
      (gl-vertex-attrib-pointer (:color-attrib state) 4 GL_FLOAT false 0
                                color-data-offset)
      (gl-bind-buffer GL_ELEMENT_ARRAY_BUFFER index-buffer-object))
    
    (assoc state
      :vao vao
      :vertex-buffer-object vertex-buffer-object
      :index-buffer-object index-buffer-object)))

(defn setup
  []
  (print-info *ns*)
  (let [shaders {:vert "tut06/PosColorLocalTransform.vert"
                 :frag "tut06/ColorPassthrough.frag"}
        {:keys [the-program] :as state} (init-program shaders)
        state
        (assoc state
          :position-attrib (gl-get-attrib-location the-program "position")
          :color-attrib (gl-get-attrib-location the-program "color")
          :model (gl-get-uniform-location the-program "modelToCameraMatrix")
          :clip (gl-get-uniform-location the-program "cameraToClipMatrix"))
        z-near 1.0
        z-far 100.0
        camera-to-clip-matrix (doto (mat4/set-zero! (mat4))
                                (mat4/set-matrix! 0 0 frustum-scale)
                                (mat4/set-matrix! 1 1 frustum-scale)
                                (mat4/set-matrix! 2 2 (/ (+ z-far z-near)
                                                         (- z-near z-far)))
                                (mat4/set-matrix! 2 3 -1.0)
                                (mat4/set-matrix! 3 2 (/ (* 2 z-far z-near)
                                                         (- z-near z-far))))
        mat4-buffer (buffers/create-float-buffer mat4-size)
        buf (fill-and-flip-buffer camera-to-clip-matrix mat4-buffer)]
    (with-program the-program
      (gl-uniform-matrix4 (:clip state) false buf))

    (let [new-state (initialize-vao state)]
      
      (gl-enable GL_CULL_FACE)
      (gl-cull-face GL_BACK)
      (gl-front-face GL_CW)

      (gl-enable GL_DEPTH_TEST)
      (gl-depth-mask true)
      (gl-depth-func GL_LEQUAL)
      (gl-depth-range 0.0 1.0)
      
      (assoc new-state
        :mat4-buffer mat4-buffer
        :camera-to-clip-matrix camera-to-clip-matrix
        :armature (arm/armature)))))

(defn update-armature
  [state]
  (cond-> state
    (kb/key-down? kb/KEY_A) (update-in [:armature] arm/adjust-base false)
    (kb/key-down? kb/KEY_D) (update-in [:armature] arm/adjust-base true)
    
    (kb/key-down? kb/KEY_W) (update-in [:armature] arm/adjust-upper-arm false)
    (kb/key-down? kb/KEY_S) (update-in [:armature] arm/adjust-upper-arm true)
    
    (kb/key-down? kb/KEY_R) (update-in [:armature] arm/adjust-lower-arm false)
    (kb/key-down? kb/KEY_F) (update-in [:armature] arm/adjust-lower-arm true)

    (kb/key-down? kb/KEY_T) (update-in [:armature] arm/adjust-wrist-pitch false)
    (kb/key-down? kb/KEY_G) (update-in [:armature] arm/adjust-wrist-pitch true)

    (kb/key-down? kb/KEY_Z) (update-in [:armature] arm/adjust-wrist-roll false)
    (kb/key-down? kb/KEY_C) (update-in [:armature] arm/adjust-wrist-roll true)

    (kb/key-down? kb/KEY_Q) (update-in [:armature] arm/adjust-finger-open true)
    (kb/key-down? kb/KEY_E) (update-in [:armature] arm/adjust-finger-open false)
    ))

(defn update
  [{:keys [armature] :as state}]
  (let [armature-keys (select-keys armature [:ang-base :ang-upper-arm
                                             :ang-lower-arm :ang-wrist-pitch
                                             :ang-wrist-roll :ang-finger-open])]
    (reduce (fn [state key]
              (do-when
                (kb/key-down? kb/KEY_SPACE) (println armature-keys))
              (cond-> state
                (kb/key-down? kb/KEY_ESCAPE) (assoc :finished? true)))
            (update-armature state) (key-presses))))

(defn draw
  [{:keys [the-program model mat4-buffer armature]
    :as state}]
  (gl-clear-color 0.0 0.0 0.0 0.0)
  (gl-clear-depth 1.0)
  (gl-clear (bit-or GL_COLOR_BUFFER_BIT GL_DEPTH_BUFFER_BIT))
  (arm/draw state))

(defn resize
  [{:keys [the-program camera-to-clip-matrix clip mat4-buffer] :as state}]
  (doto camera-to-clip-matrix
    (mat4/set-matrix! 0 0 (/ frustum-scale (/ (width) (height))))
    (mat4/set-matrix! 1 1 frustum-scale))
  (with-program the-program
    (gl-uniform-matrix4 clip false (fill-and-flip-buffer camera-to-clip-matrix
                                                         mat4-buffer)))
  (gl-viewport 0 0
               (* (width) (d/pixel-scale-factor))
               (* (height) (d/pixel-scale-factor))))

(defsketch tut06
  :setup setup
  :update update
  :draw draw
  :resize resize
  :frame-rate 60
  :size [700 700]
  :title "tut06"
  :features #{:resizable})
