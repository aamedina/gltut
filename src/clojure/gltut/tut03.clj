(ns gltut.tut03
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

(defn compute-position-offsets
  [{:keys [elapsed-time] :as state}]
  (let [loop-duration 5.0
        scale (/ (* 3.14159 2.0) loop-duration)
        elapsed-time (/ elapsed-time 1000.0)
        t (mod elapsed-time loop-duration)]
    (assoc state
      :x-offset (cos (* t scale 0.5))
      :y-offset (sin (* t scale 0.5)))))

(defn gen-buffers
  [data]
  (let [data (buffer-of :float (float-array data))
        vertex-buffer-object (gl-gen-buffers)]
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (gl-buffer-data GL_ARRAY_BUFFER data GL_STATIC_DRAW)
    (gl-bind-buffer GL_ARRAY_BUFFER 0)
    vertex-buffer-object))

(defn setup
  []
  (print-info *ns*)
  (let [vertex-data [0.25 0.25 0 1
                     0.25 -0.25 0 1
                     -0.25 -0.25 0 1]
        vertex-buffer-object (gen-buffers vertex-data)]
    (-> {:vert "tut02/FragPosition.vert"
         :frag "tut02/FragPosition.frag"
         :start-time (System/nanoTime)}
        (assoc :vertex-buffer-object vertex-buffer-object)
        (init-program)
        (assoc :vao (doto (gl-gen-vertex-arrays)
                      (gl-bind-vertex-array))))))

(defn update
  [{:keys [start-time last-frame-timestamp] :as state}]
  (let [elapsed-time (/ (- (System/nanoTime) start-time) (float 1000000.0))
        now (System/nanoTime)
        last-frame-duration (/ (- now (or last-frame-timestamp 0))
                               (float 1000000.0))]
    (-> (tut01/update state)
        (assoc :elapsed-time elapsed-time
               :last-frame-duration last-frame-duration
               :last-frame-timestamp now)
        compute-position-offsets
        ((fn [{:keys [the-program] :as state}]
           (assoc state
             :offset-location (gl-get-uniform-location the-program "offset")))))))

(defn draw
  [{:keys [vertex-buffer-object the-program x-offset y-offset] :as state}]
  (gl-clear-color 0 0 0 0)
  (gl-clear GL_COLOR_BUFFER_BIT)
  (with-program the-program
    (gl-uniform2f (:offset-location state) x-offset y-offset)
    (gl-bind-buffer GL_ARRAY_BUFFER vertex-buffer-object)
    (with-vertex-attrib-arrays [0]
      (gl-vertex-attrib-pointer 0 4 GL_FLOAT false 0 0)
      (gl-draw-arrays GL_TRIANGLES 0 3))))

(defn tut03
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
       :title "tut03"))))
