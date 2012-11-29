(ns rpi-challenger.http
  (:require [http.async.client :as http])
  (:import [org.slf4j LoggerFactory Logger]))

(def ^:dynamic logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(defn- nil-or-str
  [object]
  (if (nil? object)
    nil
    (str object)))

(defn- simple-http-response
  [response]
  {:body (http/string response)
   :status (http/status response)
   :error (nil-or-str (http/error response))})

(defn ^:dynamic post-request
  [url body]
  (try
    (with-open [client (http/create-client)]
      (-> (http/POST client url :body body :timeout 1000)
        http/await
        simple-http-response))
    (catch InterruptedException e
      (throw e))
    (catch Throwable t
      (.warn logger (str "Failed to POST to " url) t)
      {:body nil
       :status nil
       :error (.toString t)})))
