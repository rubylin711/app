package com.prime.homeplus.tv.data;

import java.util.List;

public class MiniEpgAdBean {

    private String blockName;
    private String entryTime;
    private String pathPrefix;
    private List<ChildrenBean> children;

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public List<ChildrenBean> getChildren() {
        return children;
    }

    public void setChildren(List<ChildrenBean> children) {
        this.children = children;
    }

    public static class ChildrenBean {

        private AssetTypeBean assetType;
        private String childName;
        private String durationValue;
        private String playModeSubType;
        private String playModeType;
        private String playModeValue;
        private String videoValue;

        public AssetTypeBean getAssetType() {
            return assetType;
        }

        public void setAssetType(AssetTypeBean assetType) {
            this.assetType = assetType;
        }

        public String getChildName() {
            return childName;
        }

        public void setChildName(String childName) {
            this.childName = childName;
        }

        public String getDurationValue() {
            return durationValue;
        }

        public void setDurationValue(String durationValue) {
            this.durationValue = durationValue;
        }

        public String getPlayModeSubType() {
            return playModeSubType;
        }

        public void setPlayModeSubType(String playModeSubType) {
            this.playModeSubType = playModeSubType;
        }

        public String getPlayModeType() {
            return playModeType;
        }

        public void setPlayModeType(String playModeType) {
            this.playModeType = playModeType;
        }

        public String getPlayModeValue() {
            return playModeValue;
        }

        public void setPlayModeValue(String playModeValue) {
            this.playModeValue = playModeValue;
        }

        public String getVideoValue() {
            return videoValue;
        }

        public void setVideoValue(String videoValue) {
            this.videoValue = videoValue;
        }

        public static class AssetTypeBean {
            private List<ImageBean> image;

            public List<ImageBean> getImage() {
                return image;
            }

            public void setImage(List<ImageBean> image) {
                this.image = image;
            }

            public static class ImageBean {
                private String actionType;
                private String actionValue;
                private String assetValue;

                public String getActionType() {
                    return actionType;
                }

                public void setActionType(String actionType) {
                    this.actionType = actionType;
                }

                public String getActionValue() {
                    return actionValue;
                }

                public void setActionValue(String actionValue) {
                    this.actionValue = actionValue;
                }

                public String getAssetValue() {
                    return assetValue;
                }

                public void setAssetValue(String assetValue) {
                    this.assetValue = assetValue;
                }
            }
        }
    }
}
