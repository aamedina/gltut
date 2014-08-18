(defproject gltut "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [lwcgl "0.1.0-SNAPSHOT"]
                 [org.lwjgl.lwjgl/lwjgl "2.9.2-SNAPSHOT"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.9.2-SNAPSHOT"
                  :classifier "natives-osx"
                  :native-prefix ""]
                 [org.lwjgl.lwjgl/lwjgl_util "2.9.2-SNAPSHOT"]
                 [org.lwjgl.lwjgl/lwjgl_util_applet "2.9.2-SNAPSHOT"]
                 [riddley "0.1.7"]]
  :main ^:skip-aot gltut.core
  :profiles {:uberjar {:aot :all}}
  :source-paths ["src/clojure"]
  :resource-paths ["resources" "src/glsl"])
