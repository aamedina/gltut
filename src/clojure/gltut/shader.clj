(ns gltut.shader
  (:require [lwcgl.opengl.v41 :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn info-log
  [x]
  (gl-get-shader-info-log x (gl-get-shaderi x GL_INFO_LOG_LENGTH)))

(defn throw-on-shader-compilation-error
  [shader]
  (when (== (gl-get-shaderi shader GL_LINK_STATUS) GL_FALSE)
    (gl-delete-shader shader)
    (throw (ex-info (info-log shader) {:shader shader}))))

(defn throw-on-program-compilation-error
  [program]
  (when (== (gl-get-programi program GL_LINK_STATUS) GL_FALSE)
    (gl-delete-program program)
    (throw (ex-info (info-log program) {:program program}))))

(defn compile-shader
  [shader-type source]
  (let [shader (gl-create-shader shader-type)]
    (gl-shader-source shader source)
    (gl-compile-shader shader)
    (throw-on-shader-compilation-error shader)
    shader))

(defn throw-on-program-compilation-error
  [program]
  (when (== (gl-get-programi program GL_LINK_STATUS) GL_FALSE)
    (gl-delete-program program)
    (let [log-length (gl-get-shaderi program GL_INFO_LOG_LENGTH)
          info (gl-get-shader-info-log program log-length)]
      (throw (ex-info info {:program program})))))

(defn link-program
  ([shaders] (link-program (gl-create-program) shaders))
  ([program shaders]     
     (doseq [shader shaders]
       (gl-attach-shader program shader))    
     (gl-link-program program)     
     (throw-on-program-compilation-error program)    
     (doseq [shader shaders]
       (gl-detach-shader program shader))     
     program))

(defn load-shader
  ([path] (load-shader (case (first (re-seq #"\.\w*" path))
                         ".vert" GL_VERTEX_SHADER
                         ".frag" GL_FRAGMENT_SHADER) path))
  ([shader-type path]
     (compile-shader shader-type (slurp (io/resource path)))))

(defn create-program
  [& shaders]
  (try
    (link-program shaders)
    (finally
      (doseq [shader shaders]
        (gl-delete-shader shader)))))

(defn- to-camel-case
  [s]
  (let [[prefix & more] (str/split (name s) #"-")]
    (apply str prefix (map str/capitalize more))))

(defn program
  [vertex fragment uniforms]
  (let [program (create-program (load-shader vertex) (load-shader fragment))]
    {:program program
     :uniforms (into {} (for [uniform uniforms
                              :let [n (to-camel-case uniform)]]
                          [uniform (gl-get-uniform-location program n)]))}))
