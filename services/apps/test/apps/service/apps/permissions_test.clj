(ns apps.service.apps.permissions-test
  (:use [apps.user :only [user-from-attributes]]
        [clojure.test]
        [clostache.parser :only [render]]
        [kameleon.uuids :only [uuidify]])
  (:require [apps.clients.iplant-groups :as ipg]
            [apps.clients.permissions :as perms-client]
            [apps.service.apps :as apps]
            [apps.service.workspace :as workspace]
            [apps.test-fixtures :as tf]
            [apps.util.config :as config]
            [clojure.tools.logging :as log]
            [korma.core :as sql]
            [permissions-client.core :as pc]))

(defn create-user [i]
  (let [username (str "testde" i)]
    {:user       username
     :first-name username
     :last-name  username
     :email      (str username "@mail.org")}))

(defn create-user-map []
  (->> (take 10 (iterate inc 1))
       (mapv (comp (juxt (comp keyword :user) identity) create-user))
       (into {})))

(def users (create-user-map))

(defn get-user [k]
  (user-from-attributes (users k)))

(def app-definition
  {:description "Testing"
   :groups      [{:label      "Parameters"
                  :name       "Parameters"
                  :parameters [{:description     "Select an input file."
                                :file_parameters {:data_source        "file"
                                                  :file_info_type     "File"
                                                  :format             "Unspecified"}
                                :isVisible       true
                                :label           "Input file"
                                :name            ""
                                :omit_if_blank   false
                                :order           0
                                :required        true
                                :type            "FileInput"
                                :validators      []}
                               {:defaultValue    "out.txt"
                                :description     ""
                                :file_parameters {:data_source    "stdout"
                                                  :file_info_type "File"
                                                  :format         "Unspecified"
                                                  :retain         true}
                                :isVisible       true
                                :label           "Output file name"
                                :name            ""
                                :omit_if_blank   false
                                :order           1
                                :required        false
                                :type            "FileOutput"
                                :valicators      []
                                :value           "out.txt"}]}]
   :name  "Test App"
   :tools [{:attribution ""
            :description "Word Count"
            :id          (uuidify "85cf7a33-386b-46fe-87c7-8c9d59972624")
            :location    ""
            :name        "wc"
            :type        "executable"
            :version     "0.0.1"}]})

(def ^:dynamic test-app nil)
(def ^:dynamic public-apps nil)
(def ^:dynamic beta-apps nil)

(defn with-test-app [f]
  (binding [test-app (apps/add-app (get-user :testde1) app-definition)]
    (f)
    (apps/permanently-delete-apps (get-user :testde1) {:app_ids [(:id test-app)]})))

(defn with-workspaces [f]
  (dorun (map (comp workspace/get-workspace get-user) (keys users)))
  (f))

(defn register-public-apps []
  (for [app (sql/select :app_listing (sql/where {:is_public true :deleted false}))]
    (do (pc/grant-permission (config/permissions-client) "app" (:id app) "group" (ipg/grouper-user-group-id) "read")
        app)))

(defn category-name-subselect [category-name]
  (sql/subselect [:app_category_app :aca]
                 (sql/join [:app_categories :c] {:aca.app_category_id :c.id})
                 (sql/fields :aca.app_id)))

(defn load-beta-apps []
  (sql/select [:app_listing :a]
              (sql/where {:a.id        [in (category-name-subselect "Beta")]
                          :a.is_public true
                          :a.deleted   false})))

(defn with-public-apps [f]
  (binding [public-apps (into [] (register-public-apps))
            beta-apps   (into [] (load-beta-apps))]
    (f)))

(use-fixtures :once tf/run-integration-tests tf/with-test-db tf/with-config with-workspaces)
(use-fixtures :each with-public-apps with-test-app)

(defn find-category [category-name [cat & cats]]
  (if-not (or (nil? cat) (= (:name cat) category-name))
    (or (find-category category-name (:categories cat))
        (recur category-name cats))
    cat))

(defn get-category [user category-name]
  (find-category category-name (:categories (apps/get-app-categories user {}))))

(defn get-admin-category [user category-name]
  (find-category category-name (:categories (apps/get-admin-app-categories user {}))))

(deftest test-app-search
  (let [{username :shortUsername :as user} (get-user :testde1)]
    (is (= 1 (:app_count (apps/search-apps user {:search "Test App"}))))
    (is (= 1 (count (:apps (apps/search-apps user {:search "Test App"})))))
    (perms-client/unshare-app (:id test-app) "user" username)
    (is (= 0 (:app_count (apps/search-apps user {:search "Test App"}))))
    (is (= 0 (count (:apps (apps/search-apps user {:search "Test App"})))))
    (pc/grant-permission (config/permissions-client) "app" (:id test-app) "user" username "own")
    (is (= 1 (:app_count (apps/search-apps user {:search "Test App"}))))
    (is (= 1 (count (:apps (apps/search-apps user {:search "Test App"})))))))

(deftest test-app-category-listing-counts
  (let [{username :shortUsername :as user} (get-user :testde1)
        dev-category-id                    (:id (get-category user "Apps under development"))
        beta-category-id                   (:id (get-category user "Beta"))
        group-id                           (ipg/grouper-user-group-id)]
    (is (= 1 (:app_count (apps/list-apps-in-category user dev-category-id {}))))
    (is (= (count beta-apps) (:app_count (apps/list-apps-in-category user beta-category-id {}))))
    (perms-client/unshare-app (:id test-app) "user" username)
    (is (= 0 (:app_count (apps/list-apps-in-category user dev-category-id {}))))
    (pc/grant-permission (config/permissions-client) "app" (:id test-app) "user" username "own")
    (is (= 1 (:app_count (apps/list-apps-in-category user dev-category-id {}))))
    (is (= (count beta-apps) (:app_count (apps/list-apps-in-category user beta-category-id {}))))
    (pc/revoke-permission (config/permissions-client) "app" (:id (first beta-apps)) "group" group-id)
    (is (= 1 (:app_count (apps/list-apps-in-category user dev-category-id {}))))
    (is (= (dec (count beta-apps)) (:app_count (apps/list-apps-in-category user beta-category-id {}))))))

(deftest test-app-hierarchy-counts
  (let [{username :shortUsername :as user} (get-user :testde1)
        group-id                           (ipg/grouper-user-group-id)]
    (is (= 1 (:app_count (get-category user "Apps under development"))))
    (is (= (count beta-apps) (:app_count (get-category user "Beta"))))
    (perms-client/unshare-app (:id test-app) "user" username)
    (is (= 0 (:app_count (get-category user "Apps under development"))))
    (is (= (count beta-apps) (:app_count (get-category user "Beta"))))
    (pc/grant-permission (config/permissions-client) "app" (:id test-app) "user" username "own")
    (is (= 1 (:app_count (get-category user "Apps under development"))))
    (is (= (count beta-apps) (:app_count (get-category user "Beta"))))
    (pc/revoke-permission (config/permissions-client) "app" (:id (first beta-apps)) "group" group-id)
    (is (= 1 (:app_count (get-category user "Apps under development"))))
    (is (= (dec (count beta-apps)) (:app_count (get-category user "Beta"))))))

(deftest test-admin-app-hierarchy-counts
  (let [{username :shortUsername :as user} (get-user :testde1)
        group-id                           (ipg/grouper-user-group-id)]
    (is (= (count beta-apps) (:app_count (get-admin-category user "Beta"))))
    (pc/revoke-permission (config/permissions-client) "app" (:id (first beta-apps)) "group" group-id)
    (is (= (dec (count beta-apps)) (:app_count (get-admin-category user "Beta"))))))

(defn find-app [listing app-id]
  (first (filter (comp (partial = app-id) :id) (:apps listing))))

(deftest test-app-category-listing
  (let [{username :shortUsername :as user} (get-user :testde1)
        beta-category-id                   (:id (get-category user "Beta"))
        group-id                           (ipg/grouper-user-group-id)
        app-id                             (:id (first beta-apps))]
    (is (find-app (apps/list-apps-in-category user beta-category-id {}) app-id))
    (pc/revoke-permission (config/permissions-client) "app" (:id (first beta-apps)) "group" group-id)
    (is (nil? (find-app (apps/list-apps-in-category user beta-category-id {}) app-id)))))
