package com.wanted.codebombalms.global.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcp")
public class GcpStorageProperties {

    private String projectId;
    private final Storage storage = new Storage();


    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Storage getStorage() {
        return storage;
    }


    public static class Storage {

        private String bucket;
        private String datasetPrefix = "problem_dataset";
        private String badgeImagePrefix = "badge_image";
        private String courseThumbnailPrefix = "course_thumbnail_images";
        private String lectureMaterialPrefix = "lecture_materials";

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getDatasetPrefix() {
            return datasetPrefix;
        }

        public void setDatasetPrefix(String datasetPrefix) {
            this.datasetPrefix = datasetPrefix;
        }

        public String getBadgeImagePrefix() {
            return badgeImagePrefix;
        }

        public void setBadgeImagePrefix(String badgeImagePrefix) {
            this.badgeImagePrefix = badgeImagePrefix;
        }

        public String getCourseThumbnailPrefix() {
            return courseThumbnailPrefix;
        }

        public void setCourseThumbnailPrefix(String courseThumbnailPrefix) {
            this.courseThumbnailPrefix = courseThumbnailPrefix;
        }

        public String getLectureMaterialPrefix() {
            return lectureMaterialPrefix;
        }

        public void setLectureMaterialPrefix(String lectureMaterialPrefix) {
            this.lectureMaterialPrefix = lectureMaterialPrefix;
        }
    }

}
