(ns data-info.routes.domain.common
  (:use [common-swagger-api.schema :only [describe NonBlankString ->optional-param]])
  (:require [heuristomancer.core :as hm]
            [schema.core :as s])
  (:import [java.util UUID]))

(defn get-error-code-block
  [& error-codes]
  (str "\n\n#### Error Codes:\n    " (clojure.string/join "\n    " error-codes)))

(def DataIdPathParam (describe UUID "The data item's UUID"))

(s/defschema Paths
  {:paths (describe [NonBlankString] "A list of iRODS paths")})

(s/defschema OptionalPaths
  (-> Paths
    (->optional-param :paths)))

(s/defschema DataIds
  {:ids (describe [UUID] "A list of iRODS data-object UUIDs")})

(s/defschema OptionalPathsOrDataIds
  (-> (merge DataIds Paths)
      (->optional-param :paths)
      (->optional-param :ids)))

(def ValidInfoTypesEnum (apply s/enum (hm/supported-formats)))
(def ValidInfoTypesEnumPlusBlank (apply s/enum (conj (hm/supported-formats) "")))
