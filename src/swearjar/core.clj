(ns swearjar.core
  (:require [clj-sockets.core :as socket]
            [swearjar.swear :as swear]
            [clojure.string :as string]))

(def conn (atom nil))

(defn close-connetion! []
  (when-let [c @conn]
    (socket/close-socket c)
    (reset! conn nil)))

(defn connect-user! [id]
  (when-let [c @conn]
    (close-connetion!))
  (reset! conn (socket/create-socket "localhost" 1234))
  (socket/write-line @conn (str "USER " id)))

(defn send-message [message]
  (if-let [c @conn]
    (socket/write-line c (str "MSG " message))
    (throw (Exception. "no connection available!"))))

(def scoreboard-command "!scoreboard")

(defn print-scoreboard []
  (doall (map send-message (swear/stringify-pottymouths))))

(defn parse-line [line]
  (let [words (string/split line #" ")
        nick (string/replace (first words) #":$" "")
        content (rest words)]
      (if (= scoreboard-command (first content))
        (print-scoreboard)
        (swear/digest-words nick content))))

(defn -main
  [& args]
  (do
    (connect-user! "swearjar")
    (println "connected.")

    (send-message "Watch your language.")

    (loop [line (socket/read-line @conn)]
      (println line)
      (parse-line line)
      (when-not (.isClosed @conn)
        (recur (socket/read-line @conn)))
      )

    ;(close-connetion!)
    ))

