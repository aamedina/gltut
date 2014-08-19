(ns gltut.math.mat4
  (:refer-clojure :exclude [identity]))

(def identity
  [[1.0 0.0 0.0 0.0]
   [0.0 1.0 0.0 0.0]
   [0.0 0.0 1.0 0.0]
   [0.0 0.0 0.0 1.0]])

(defn add
  {:inline-arities (fn [n] (> n 1))
   :inline (fn
             ([] identity)
             ([x] x)
             ([x y]
                `(let [[[x00# x01# x02# x03#]
                        [x10# x11# x12# x13#]
                        [x20# x21# x22# x23#]
                        [x30# x31# x32# x33#]] ~x
                        [[y00# y01# y02# y03#]
                         [y10# y11# y12# y13#]
                         [y20# y21# y22# y23#]
                         [y30# y31# y32# y33#]] ~y]
                   [[(+ x00# y00#) (+ x01# y01#) (+ x02# y02#) (+ x03# y03#)]
                    [(+ x10# y10#) (+ x11# y11#) (+ x12# y12#) (+ x13# y13#)]
                    [(+ x20# y20#) (+ x21# y21#) (+ x22# y22#) (+ x23# y23#)]
                    [(+ x30# y30#) (+ x31# y31#) (+ x32# y32#) (+ x33# y33#)]]))
             ([x y & more]
                (reduce (fn [a b] `(add ~a ~b)) `(add ~x ~y) more)))}
  ([] identity)
  ([x] x)
  ([[[x00 x01 x02 x03]
     [x10 x11 x12 x13]
     [x20 x21 x22 x23]
     [x30 x31 x32 x33]]
    [[y00 y01 y02 y03]
     [y10 y11 y12 y13]
     [y20 y21 y22 y23]
     [y30 y31 y32 y33]]]
     [[(+ x00 y00) (+ x01 y01) (+ x02 y02) (+ x03 y03)]
      [(+ x10 y10) (+ x11 y11) (+ x12 y12) (+ x13 y13)]
      [(+ x20 y20) (+ x21 y21) (+ x22 y22) (+ x23 y23)]
      [(+ x30 y30) (+ x31 y31) (+ x32 y32) (+ x33 y33)]])
  ([x y & more]
     (reduce (fn [a b] (add a b)) (add x y) more)))

(defn sub
  ([x] (mapv (fn [r] (mapv - r)) x))
  ([x y] (mapv (fn [r1 r2] (mapv - r1 r2)) x y))
  ([x y & more]
     (reduce sub (sub x y) more)))

(defn determinant
  [[[m00 m01 m02 m03]
    [m10 m11 m12 m13]
    [m20 m21 m22 m23]
    [m30 m31 m32 m33]]]
  (-> (* m00 (- (+ (* m11 m22 m33) (* m12 m23 m31) (* m13 m21 m32))
                (* m13 m22 m31)
                (* m11 m23 m32)
                (* m12 m21 m33)))
      (- (* m01 (- (+ (* m10 m22 m33) (* m12 m23 m30) (* m13 m20 m32))
                   (* m13 m22 m30)
                   (* m10 m23 m32)
                   (* m12 m20 m33))))
      (+ (* m02 (- (+ (* m10 m21 m33) (* m11 m23 m30) (* m13 m20 m31))
                   (* m13 m21 m30)
                   (* m10 m23 m31)
                   (* m11 m20 m33))))
      (- (* m03 (- (+ (* m10 m21 m32) (* m11 m22 m30) (* m12 m20 m31))
                   (* m12 m21 m30)
                   (* m10 m22 m31)
                   (* m11 m20 m32))))))

(defn transpose
  [[[m00 m01 m02 m03]
    [m10 m11 m12 m13]
    [m20 m21 m22 m23]
    [m30 m31 m32 m33]]]
  [[m00 m10 m20 m30]
   [m01 m11 m21 m31]
   [m02 m12 m22 m32]
   [m03 m13 m23 m33]])
