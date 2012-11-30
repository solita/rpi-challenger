(ns rpi-challenger.http
  (:require [http.async.client :as http])
  (:import [org.slf4j LoggerFactory Logger]))

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

(defonce ^:private client (http/create-client))

(defn ^:dynamic post-request
  [url body]
  (try
    (-> (http/POST client url :body body :timeout 1000)
      http/await
      simple-http-response)
    (catch InterruptedException e
      (throw e))
    (catch Throwable t
      {:body nil
       :status nil
       :error (.toString t)})))

(defn error?
  [response]
  (not (nil? (:error response))))
