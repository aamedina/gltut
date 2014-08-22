(defproject gltut "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [lwcgl "0.1.0-SNAPSHOT"]
                 [euclidean "0.2.0"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.9.2-SNAPSHOT"
                  :classifier "natives-osx"
                  :native-prefix ""]
                 [riddley "0.1.7"]
                 [criterium "0.4.3"]]
  :main ^:skip-aot gltut.core
  :profiles {:uberjar {:aot :all}}
  :source-paths ["src/clojure"]
  :resource-paths ["resources" "src/glsl"]
  :jvm-opts ^:replace ["-Dorg.lwjgl.opengl.Display.enableHighDPI=true"])
