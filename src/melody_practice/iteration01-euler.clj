(ns melody-practice.iteration01
  (:use [overtone.live]
        [overtone.inst.sampled-piano])
  (:require [clojure.string :as str]))


;; CLOJURE-y STUFF
(defn drop-nth [n coll]
   (keep-indexed #(if (not= %1 n) %2) coll))

(defn sublist-at-doubles [masterlist]
  (let [coll masterlist
        masterlist (into [] masterlist)]
    (loop [coll coll
           index-1 0
           index-2 0
           acc []]
      (if (< (count coll) 2)
        (conj acc (subvec masterlist index-1))
        (recur
         (if (= (first coll) (fnext coll))
           (rest (rest coll))
           (rest coll))
         (if (= (first coll) (fnext coll))
           index-2
           index-1)
         (if (= (first coll) (fnext coll))
           (+ 2 index-2)
           (inc index-2))
         (if (= (first coll) (fnext coll))
           (conj acc (subvec masterlist index-1 index-2))
           acc))))))

(defn sublist-at-triples [masterlist]
  (let [coll masterlist
        masterlist (into [] masterlist)]
    (loop [coll coll
           index-1 0
           index-2 0
           acc []]
      (if (< (count coll) 3)
        (conj acc (subvec masterlist index-1))
        (recur
         (if (= (first coll) (fnext coll) (fnext (rest coll)))
           (rest (rest (rest coll)))
           (rest coll))
         (if (= (first coll) (fnext coll) (fnext (rest coll)))
           index-2
           index-1)
         (if (= (first coll) (fnext coll) (fnext (rest coll)))
           (+ 3 index-2)
           (inc index-2))
         (if (= (first coll) (fnext coll) (fnext (rest coll)))
           (conj acc (subvec masterlist index-1 index-2))
           acc))))))

(defn upcoming-double? [coll]
  (if (= (first coll) (fnext coll))
    true
    false))


;; MATH STUFF
(def digits (map read-string (remove #{"."} (map str (str/trim (str/trim-newline (slurp "10k-euler.txt")))))))
(def mini-digits (take 250 digits))
(def freq-1 (frequencies digits))
(def freq-2 (frequencies (concat (partition 2 digits) (partition 2 (rest digits)))))
(def freq-3 (frequencies (concat (partition 3 digits) (partition 3 (rest digits)) (partition 3 (rest (rest digits))))))
(def m-one (apply assoc {} (interleave (map key freq-1) (map #(/ (val %) (reduce + (map val freq-1))) freq-1))))
(def m-two (apply assoc {} (interleave (map key freq-2) (map #(/ (val %) (freq-1 (first (key %)))) freq-2))))
(def m-three (apply assoc {} (interleave (map key freq-3) (map #(/ (val %) (freq-2 (take 2 (key %)))) freq-3))))
(def mini-n-sub-by-doubles (sublist-at-doubles mini-digits))
(def mini-n-sub-by-triples (sublist-at-triples mini-digits))

(defn variance-of [coll]
  "Finds the variance of a collection of numbers
   will typically use (map val markov-n-deep), e.g., (map val m-two) as the collection"
  (let [mean (/ (reduce + coll) (count coll))
        prevar (map #(- % mean) coll)]
    (/ (reduce + (map #(* % %) prevar)) (- (count coll) 1))))

(defn stdev-of [coll]
  "Finds the standard deviation of a collection of numbers
   will typically use (map val markov-n-deep), e.g., (map val m-two) as the collection"
  (Math/sqrt (variance-of coll)))

(defn unusual? [element coll degree]
  "Determines if a given number (here, it's element) is outside of a certain range.
   The range is from some degree of standard deviations below to the same degree of standard
   deviations above the mean.
   In this context: coll will probably be something like (map val m-num)
   Returns true or false."
  (let [mean (/ (reduce + coll) (count coll))
        stdev (stdev-of coll)]
    (if (> element mean)
      (if (>= element (+ (* degree stdev) mean))
        true
        false)
      (if (<= element (- mean (* degree stdev)))
        true
        false))))


;; MUSIC STUFF
(def mel-major [11 12 14 16 17 19 21 23 24 26])
(def mel-minor [10 12 14 15 17 19 20 22 24 26])
(def har-major-1 [12 14 16 19 21 23 24 26 28 31])
(def har-major-2 [12 14 16 18 19 21 23 24 26 28])
(def har-minor-1 [12 14 15 19 20 22 24 26 27 31])


(defn get-scale-val [digit scale]
  ((zipmap (range) scale) digit))

(defn get-length-multiplier [value m-chain]
  "While I originally intended this to work for just two, it will work for
   potentially three or more--however many deep the markov chains are built."
  (cond
   (unusual? value m-chain 1.52) 2.0
   (unusual? value m-chain 0.88) 1.5
   :else 1.0))

(defn mel-note-dur [coll npms]
  "Uses data, such as the current digit, the remainder of the list, and
   the notes per millisecond (i.e., npms) to determine how many milliseconds
   the note in question should be."
  (if (<= (count coll) 1)
    (int (* npms 3.0))
    (int (* npms (get-length-multiplier (m-two (take 2 coll)) (map val m-two))))))

(defn get-mel-segment-length [list npms-base]
  (loop [list list
         acc []]
    (if (empty? list)
      (reduce + acc)
      (recur (rest list) (cons (mel-note-dur list npms-base) acc)))))

(defn get-note-distance [n-1 n-2 the-scale]
  (- (get-scale-val n-1 the-scale) (get-scale-val n-2 the-scale)))

(defn get-pitch-adjust-for-segment [list mel-scale pitch-adjust maxim minim distance]
  (loop [list list
         pitch-adjust pitch-adjust
         acc []]
    (if (empty? list)
      acc
      (recur (rest list)
             (cond
               (empty? (rest list)) pitch-adjust
               (< (get-note-distance (first list) (first (rest list)) mel-scale) (- distance))
                 (if (> pitch-adjust minim)
                   (- pitch-adjust 12)
                   pitch-adjust)
               (> (get-note-distance (first list) (first (rest list)) mel-scale) distance)
                 (if (< pitch-adjust maxim)
                   (+ pitch-adjust 12)
                   pitch-adjust)
               :else pitch-adjust)
             (conj acc pitch-adjust)))))

(defn get-pitch-adjust-for-chords [pitch-adjust dgt har-scale maxim minim]
  (let [adjustment (+ pitch-adjust (- (get-scale-val dgt har-scale) pitch-adjust))]
    (cond
     (< maxim adjustment) (- adjustment 12)
     (> minim adjustment) (+ adjustment 12)
     :else adjustment)))

(defn get-final-pitch-adjust [list mel-scale pitch-adjust maxim minim distance]
  (loop [list list
         pitch-adjust pitch-adjust]
    (if (empty? list)
      pitch-adjust
      (recur (rest list)
             (cond
               (empty? (rest list)) pitch-adjust
               (< (get-note-distance (first list) (first (rest list)) mel-scale) (- distance))
                 (if (> pitch-adjust minim)
                   (- pitch-adjust 12)
                   pitch-adjust)
               (> (get-note-distance (first list) (first (rest list)) mel-scale) distance)
                 (if (< pitch-adjust maxim)
                   (+ pitch-adjust 12)
                   pitch-adjust)
               :else pitch-adjust)))))

;; PLAY THE STUFF
(defn play-melody-segment
  [t base-ms list mel-scale pitch-adjust]
  (let [list list
        dgt (first list)
        note-ms (mel-note-dur list base-ms)
        t-next (+ t note-ms)]
    (when dgt
      (at t
          (sampled-piano (+ (first pitch-adjust) (get-scale-val dgt mel-scale)) 1.3)
        (apply-by t-next #'play-melody-segment [t-next base-ms (rest list) mel-scale (rest pitch-adjust)])))))

(defn play-harmony-segment
  [t base-ms list har-scale pitch-adjust]
  (let [list list
        dgt (first list)
        har-scale har-scale
        pitch-adjust pitch-adjust
        t-next (+ t base-ms)]
    (when dgt
      (at t
          (sampled-piano (+ pitch-adjust (get-scale-val dgt har-scale)) 0.45)
          (when (upcoming-double? list)
            (println-str dgt)
            (sampled-piano (+ 20 (mod (+ pitch-adjust (get-scale-val dgt har-scale)) 12)) 0.75)
            (sampled-piano (+ 32 (mod (+ pitch-adjust (get-scale-val dgt har-scale)) 12)) 0.80))
        (apply-by t-next #'play-harmony-segment [t-next base-ms (rest list) har-scale (get-pitch-adjust-for-chords pitch-adjust dgt har-scale 48 20)])))))

(defn play-the-thing
  [t base-ms coll mel-pitch-adjust har-pitch-adjust]
  (let [coll coll
        base-ms base-ms
        mel-length (get-mel-segment-length (first coll) base-ms)
        harm-ms (int (/ mel-length (* 4 (count (first coll)))))
        t-next (+ t mel-length)]
    (when (not (empty? (rest coll)))
      (at t
          (play-harmony-segment t
                                harm-ms
                                (take (* 4 (count (first coll))) (cycle (first coll)))
                                har-major-2
                                har-pitch-adjust)
          (play-melody-segment t
                               base-ms
                               (first coll)
                               mel-major
                               (get-pitch-adjust-for-segment (first coll) mel-major mel-pitch-adjust 94 46 9))
        (apply-by t-next #'play-the-thing [t-next
                                           (- base-ms 5)
                                           (rest coll)
                                           (get-final-pitch-adjust (first coll) mel-major mel-pitch-adjust 94 46 9)
                                           har-pitch-adjust])))))

(play-the-thing (now) 930 mini-n-sub-by-triples 46 30)

(recording-start "~/Desktop/250-euler.wav")

(recording-stop)

(stop)
