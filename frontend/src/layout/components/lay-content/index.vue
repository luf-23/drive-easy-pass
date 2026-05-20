<script setup lang="ts">
import LayFrame from "../lay-frame/index.vue";
import { useTags } from "@/layout/hooks/useTag";
import { useGlobal, isNumber } from "@pureadmin/utils";
import { h, computed, Transition, defineComponent } from "vue";
import { usePermissionStoreHook } from "@/store/modules/permission";

const props = defineProps({
  fixedHeader: Boolean
});

const { showModel } = useTags();
const { $storage, $config } = useGlobal<GlobalPropertiesApi>();

const isKeepAlive = computed(() => {
  return $config?.KeepAlive;
});

const transitions = computed(() => {
  return route => {
    return route.meta.transition;
  };
});

const hideTabs = computed(() => {
  return $storage?.configure.hideTabs;
});

const stretch = computed(() => {
  return $storage?.configure.stretch;
});

const layout = computed(() => {
  return $storage?.layout.layout === "vertical";
});

const getMainWidth = computed(() => {
  return isNumber(stretch.value)
    ? stretch.value + "px"
    : stretch.value
      ? "1440px"
      : "100%";
});

const getSectionStyle = computed(() => {
  return [
    hideTabs.value && layout ? "padding-top: 48px;" : "",
    !hideTabs.value && layout
      ? showModel.value == "chrome"
        ? "padding-top: 85px;"
        : "padding-top: 81px;"
      : "",
    hideTabs.value && !layout.value ? "padding-top: 48px;" : "",
    !hideTabs.value && !layout.value
      ? showModel.value == "chrome"
        ? "padding-top: 85px;"
        : "padding-top: 81px;"
      : "",
    props.fixedHeader
      ? ""
      : `padding-top: 0;${
          hideTabs.value
            ? "min-height: calc(100vh - 48px);"
            : "min-height: calc(100vh - 86px);"
        }`
  ];
});

const transitionMain = defineComponent({
  props: {
    route: {
      type: undefined,
      required: true
    }
  },
  render() {
    const transitionName =
      transitions.value(this.route)?.name || "fade-transform";
    const enterTransition = transitions.value(this.route)?.enterTransition;
    const leaveTransition = transitions.value(this.route)?.leaveTransition;
    return h(
      Transition,
      {
        name: enterTransition ? "pure-classes-transition" : transitionName,
        enterActiveClass: enterTransition
          ? `animate__animated ${enterTransition}`
          : undefined,
        leaveActiveClass: leaveTransition
          ? `animate__animated ${leaveTransition}`
          : undefined,
        mode: "out-in",
        appear: true
      },
      {
        default: () => [this.$slots.default()]
      }
    );
  }
});
</script>

<template>
  <section
    :class="[fixedHeader ? 'app-main' : 'app-main-nofixed-header']"
    :style="getSectionStyle"
  >
    <router-view>
      <template #default="{ Component, route }">
        <LayFrame :currComp="Component" :currRoute="route">
          <template #default="{ Comp, fullPath, frameInfo }">
            <div
              v-if="fixedHeader"
              class="content-frame"
              :style="{ maxWidth: getMainWidth }"
            >
              <div class="grow">
                <div class="main-content">
                  <transitionMain :route="route">
                    <keep-alive
                      v-if="isKeepAlive"
                      :include="usePermissionStoreHook().cachePageList"
                    >
                      <component
                        :is="Comp"
                        :key="fullPath"
                        :frameInfo="frameInfo"
                        class="page-wrapper"
                      />
                    </keep-alive>
                    <component
                      :is="Comp"
                      v-else
                      :key="fullPath"
                      :frameInfo="frameInfo"
                      class="page-wrapper"
                    />
                  </transitionMain>
                </div>
              </div>
            </div>
            <div v-else class="grow">
              <div class="main-content">
                <transitionMain :route="route">
                  <keep-alive
                    v-if="isKeepAlive"
                    :include="usePermissionStoreHook().cachePageList"
                  >
                    <component
                      :is="Comp"
                      :key="fullPath"
                      :frameInfo="frameInfo"
                      class="page-wrapper"
                    />
                  </keep-alive>
                  <component
                    :is="Comp"
                    v-else
                    :key="fullPath"
                    :frameInfo="frameInfo"
                    class="page-wrapper"
                  />
                </transitionMain>
              </div>
            </div>
          </template>
        </LayFrame>
      </template>
    </router-view>

    <!-- 页脚 -->
  </section>
</template>

<style scoped>
.app-main {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.app-main-nofixed-header {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.main-content {
  display: block;
  height: calc(100% - 32px);
  min-height: 0;
  margin: 16px;
  overflow-x: hidden;
  overflow-y: auto;
}

.content-frame {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  margin: 0 auto;
  overflow: hidden;
  transition: all 300ms cubic-bezier(0.4, 0, 0.2, 1);
}

.grow {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.page-wrapper {
  min-height: 100%;
  height: auto;
}
</style>
