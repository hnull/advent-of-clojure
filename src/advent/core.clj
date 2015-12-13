(ns advent.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.core.match :refer [match]]
   [clojure.pprint :as p]
   [clojure.set :refer [union]]
   [clojure.math.combinatorics :as combo]
   [clojure.walk :refer [prewalk postwalk]]
   [digest :refer [md5]]))

;; day 1

(def prob1 (slurp (io/resource "prob1")))

;; part 1
#_(reduce + (map {\( 1 \) -1} prob1))

;; part 2
#_(count
   (take-while #(not= % -1)
               (reductions + 0 (map {\( 1 \) -1} prob1))))

;; day 2

(def prob2
  (line-seq (io/reader (io/resource "prob2"))))

#_(first prob2)

(defn parse-dims [line]
  (map #(Integer/parseInt %)
       (string/split line #"x")))

#_(parse-dims (first prob2))

(defn side-dims [dims]
  (partition 2 1 (conj (vec dims) (first dims))))

#_ (-> (first prob2) parse-dims side-dims) 

(defn calc-area-of-present [line]
  (as-> line x
    (parse-dims x)
    (side-dims x)
    (map #(apply * %) x)
    (reduce + (reduce min x)
            (map #(* 2 %) x))))
;; part 1
#_(reduce + (map calc-area-of-present prob2)) 

(defn calc-shortest-perimiter [dimen]
  (let [dims (parse-dims dimen)]
    (as-> dims x
      (side-dims x)
      (first (sort-by #(apply * %) x))
      (+ (apply * dims)
         (* 2 (apply + x))))))

#_(calc-shortest-perimiter "29x13x26")

;; part 2
#_(reduce + (map calc-shortest-perimiter prob2))


;; Day 3

(def prob3 (slurp (io/resource "prob3"))) 

(defn positions-visited [data]
  (set
   (reductions
    #(map + %1 %2)
    (list 0 0)
    (map #(condp = %
            \^ [0 1]
            \v [0 -1]
            \> [1 0]
            \< [-1 0]) data))))
;; part 1
#_(count
    (positions-visited prob3))

;; part 2

#_(count
   (set
    (mapcat positions-visited
            (let [p (partition 2 prob3)]
              [(map first p) (map second p)]))))

;; day 4 

;; part 1
#_(first
   (filter
    #(= "00000" #_ "000000" (subs (second %) 0 5 #_6))
    (map
     (juxt identity md5)
     (map-indexed #(str %2 %1) (repeat "yzbqklnj")))))

;; part 2  change the parameters above



;;  day 5

(def prob5
  (line-seq (io/reader (io/resource "prob5"))))

(defn test-word [line]
  (and
   (not (re-matches #".*(ab|cd|pq|xy).*" line))
   (<= 3 (count (filter #(re-matches #".*(a|e|i|o|u).*" (str %)) line)))
   (some
    #(apply = %)
    (partition 2 1 line))))

(comment
  (test-word "ugknbfddgicrmopn" )
  (test-word "aaa" )
  (test-word "jchzalrnumimnmhp")
  (test-word "haegwjzuvuyypxyu")
  (test-word "dvszwmarrgswjxmb")

  ;; part 1
  (count (filter test-word prob5))
  )

(defn non-consecutive-pairs [word]
  (some
   (fn [[x & xs]] (some #(> (- % x) 1) xs))
   (vals
    (apply merge-with concat
           (map-indexed (fn [a b] {b [a]}) (partition 2 1 word))))))

(defn test-word2 [word]
  (and
   (non-consecutive-pairs word)
   (some (fn [[a _ b]] (= a b)) (partition 3 1 word))))

(comment

  (list
   (test-word2 "xxxx")
   (test-word2 "qjhvhtzxzqqjkmpb")
   (test-word2 "xxyxx")
   (test-word2 "uurcxstgmygtbstg")
   (test-word2 "ieodomkazucvgmuy"))

  ;; part 2
  
  #_(count (filter test-word2 prob5))
  )

;; day 6

(def prob6
  (line-seq (io/reader (io/resource "prob6"))))

(defn parse-command [line]
  (let [x (->> (str "[" line "]")
               read-string
               (group-by integer?))]
    [(x false) (x true)]))

#_(parse-command (first prob6)) ;; yields [[turn on through] [489 959 759 964]]

;; expand in to instructions for each position
(defn to-bytecode [[command [x1 y1 x2 y2]]]
  (map
   #(vector command %)
   (for [x (range x1 (inc x2))
         y (range y1 (inc y2))]
     [x y])))

(defn action [board [command loc]]
  (condp = (butlast command)
    '(turn on)  (assoc board loc true)
    '(turn off) (assoc board loc false)
    '(toggle)   (assoc board loc (not (board loc)))))

#_(def part1-6 (time
                (reduce action {}
                        (mapcat (comp to-bytecode parse-command)
                                prob6))))

;; part 1
#_(count (filter identity (vals part1-6)))

(defn action2 [board [command loc]]
  (update-in board [loc]
             (fnil
              (condp = (butlast command)
               '(turn on)  inc
               '(turn off) (fn [x] (max (dec x) 0))
               '(toggle)   (partial + 2))
              0)))

#_(def part2-6 (reduce action2 {} (mapcat (comp to-bytecode parse-command)
                                          prob6)))
;; part 2
#_(reduce + 0 (filter identity (vals part2-6)))

;; day 7

(def prob7
  (line-seq (io/reader (io/resource "prob7"))))

(def ex7
  (line-seq
   (io/reader
    (char-array
     "123 -> x
456 -> y
x AND y -> d
x OR y -> e
x LSHIFT 2 -> f
y RSHIFT 2 -> g
NOT x -> h
NOT y -> i"))))

(defn dependents [op command]
  (filter (complement integer?)
          (condp = op
            'NOT    (rest command)
            'OR     ((juxt first last) command)
            'AND    ((juxt first last) command)
            'LSHIFT (list (first command))
            'RSHIFT (list (first command))
            command)))

(defn parse-connection [line]
  (let [tokens (read-string (str "[" line "]"))
        output (last tokens)
        command (take-while #(not= '-> %) tokens)
        op (first (filter #(and
                            (not (integer? %))
                            (re-matches #"[A-Z]+" (name %))) command))]
    {:orig tokens
     :output output
     :command command
     :deps    (dependents op command)
     :op op}))

(def source (map parse-connection prob7))

(def source-graph (into {} (map (juxt :output identity) source) ))

(defn constant-assignment? [{:keys [op command]}]
  (and
   (nil? op)
   (= 1 (count command))
   (integer? (first command))))

(defn solvable? [source-graph {:keys [deps] :as node}]
  (or
   (and
    (not-empty deps)
    (every? (comp integer? source-graph) deps))
   (constant-assignment? node)))

(defn value [source-graph k]
  (if (integer? k) k (source-graph k)))

(defn solve [source-graph {:keys [op deps command] :as node}]
  (if-let [sol (if (constant-assignment? node)
                 (first command)
                 (let [bin-args (map (partial value source-graph)
                                     ((juxt first last) command))]
                   (condp = op
                     'NOT    (bit-not (value source-graph (last command)))
                     'OR     (apply bit-or bin-args)
                     'AND    (apply bit-and bin-args)
                     'LSHIFT (apply bit-shift-left bin-args)
                     'RSHIFT (apply bit-shift-right bin-args)
                     nil     (value source-graph (first command)))))]
    (assoc source-graph (:output node) sol)
    source-graph))

(defn update-step [source-graph]
  (->> source-graph
       vals
       (filter (partial solvable? source-graph))
       (reduce solve source-graph)))

; part 1
#_((->> source-graph
     (iterate update-step)
     (filter #(integer? (% 'a)))
     first)
   'a)

;; part 2
#_((->> (assoc source-graph 'b 3176)
        (iterate update-step)
        (filter #(integer? (% 'a)))
        first)
 'a)

(comment
  
  (:deps (parse-connection "44430 -> b"))
  
  (solvable? source-graph (parse-connection "44430 -> b"))
  
  (parse-connection "NOT dq -> dr")
  (parse-connection "dd OR do -> dp")
  (parse-connection "kk RSHIFT 3 -> km")
  (source-graph 'a))

(def prob8
  (line-seq (io/reader (io/resource "prob8"))))

(defn unescape-trunc [input]
  (drop (condp = (take 2 input)
          [\\ \"] 2 
          [\\ \\] 2 
          [\\ \x] 4 
          1)
        input))

#_(map count
       (take-while not-empty (iterate unescape-trunc "\"v\\xfb\\\"lgs\\\"kvjfywmut\\x9cr\"")))

(defn unescape-len [x]
  (+ -2
     (count (take-while not-empty
                        (iterate unescape-trunc x)))))

;; part 1
#_(reduce + 0 (map #(- (count %) (unescape-len %)) prob8))

(defn expand-len [w]
  (reduce + 2
          (map #(condp = %
                  \" 2
                  \\ 2
                  1)
               w)))

(comment
  (expand-len [\" \"])
  (expand-len [\" \a \b \c \"])a
  (expand-len [\" \a \a \a \\ \" \a \a \a \"])
  (expand-len [\" \\ \x \2 \7 \"])
  )

;; part 2
#_(reduce + 0 (map #(- (expand-len %) (count %)) prob8))

;; day 9

(def prob9
  (line-seq (io/reader (io/resource "prob9"))))

(defn tokens [x] (read-string (str "(" x ")") ))

(def dist-map
  (into {} (map (comp (fn [[a _ b _ c]] [#{a b} c])
                      tokens)
                prob9)))

(def towns (reduce union (keys dist-map)))

(comment

  towns

  ;; runtime complexity
  (* 8 7 6 5 4 3 2 1)
  
  ;; check runtime complexity 
  (count (combo/permutations towns))

  ;; part 1
  (reduce min 
          (map
           #(reduce + (map (comp dist-map set)
                           (partition 2 1 %)))
           (combo/permutations towns)))
  
  ;; part 2
  (reduce max ;; <- just change min to max here
          (map
           #(reduce + (map (comp dist-map set)
                           (partition 2 1 %)))
           (combo/permutations towns)))
  
)

;; day 10

(defn count-encode [x]
  (apply str
         (mapcat 
          (juxt count first)
          (partition-by identity x))))

;; part 1 and part 2
(count (nth (iterate count-encode "1321131112") 40))



;; day 11

(defn straight-test [x]
  (some (fn [r]
          (let [[a b c] (map byte r)]
            (and a b c (= (+ 2 a) (inc b) c))))
        (partition 3 1 x)))

(defn not-iol [x]
  (not (re-matches #".*(i|o|l).*" (apply str x)))) 

(defn has-different-pairs [x]
  (< 1
     (count (set (filter #(apply = %) (partition 2 1 x))))))

(defn valid-password? [p]
  (and
   (not-iol p)
   (has-different-pairs p)
   (straight-test p)))

(defn adjust-char [x]
  (char
   (+
    (if (<= (byte x) (byte \9)) 49 10)
    (byte x))))

(defn adjust-char-back [x]
  (char
   (-
    (byte x)
    (if (<= (byte x) (byte \j)) 49 10))))

(def base26->password #(apply str (mapv adjust-char %)))

(def password->base26 #(apply str (mapv adjust-char-back %)))

#_(adjust-char-back (adjust-char \0))
#_(adjust-char-back (adjust-char \a))

(defn long->password [i]
  (-> i
      (java.lang.Long/toString 26)
      base26->password))

(defn password->long [s]
  (-> s
      password->base26
      (java.lang.Long/valueOf 26)))

#_(password->long "cqjxjnds")
#_(password->long (long->password 500))

(defn base26-passwords [start-int]
  (filter valid-password?
          (map long->password (iterate inc start-int))))

;; part 1
#_(first (base26-passwords (password->long "cqjxjnds")))

;; part 2
#_(second (base26-passwords (password->long "cqjxxyzz")))


;; day 12

(def prob12 (slurp (io/resource "prob12")))

(def ex12 (subs prob12 0 100))

;; part 1
#_(reduce + (map #(Long/parseLong %) (re-seq #"[-]{0,1}\d+"  prob12)))

;; part 2

(defn has-red? [m]
  (some (fn [me] ((set me) "red")) m))

#_(as-> prob12 a
    (string/replace a ":" " ")
    (read-string a)           
    (prewalk #(cond
                (and (map? %) (has-red? %)) nil
                (map? %) (seq %)
                :else %) a)
    (flatten a)
    (filter integer? a)
    (reduce + a))


;; Day 13

(def prob13
  (line-seq (io/reader (io/resource "prob13"))))

(defn line-to-data13 [line]
  (as-> line x
    (string/replace x "." "")
    (str "(" x ")")
    (read-string x)
    ((juxt (comp set (juxt first last))
           #(({'lose - 'gain +} (nth % 2)) (nth % 3))) x)
    (into {} [x])))

(def seating-pair-values (reduce (partial merge-with +) (map line-to-data13 prob13)))

(def guest-names (reduce union (keys seating-pair-values)))

(defn arrangement-value [pair-values order]
  (reduce +
          (map (comp pair-values set)
               (partition 2 1 (cons (last order) order)))))

;; part 1
(reduce max (map (partial arrangement-value seating-pair-values)
                 (combo/permutations guest-names)))

;; part 2
(let [pair-values (into seating-pair-values
                        (map #(vector #{'me %} 0) guest-names))]
  (reduce max (map (partial arrangement-value pair-values)
                   (combo/permutations (cons 'me guest-names)))))













