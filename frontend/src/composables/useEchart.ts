import echarts from "@/plugins/echarts";
import type { EChartsOption } from "echarts";
import {
  onBeforeUnmount,
  onMounted,
  watch,
  type MaybeRefOrGetter,
  type Ref,
  toValue
} from "vue";

export function useEchart(
  container: Ref<HTMLElement | null | undefined>,
  option: MaybeRefOrGetter<EChartsOption | null | undefined>
) {
  let instance: ReturnType<typeof echarts.init> | null = null;
  let resizeObserver: ResizeObserver | null = null;

  const bindResize = (el: HTMLElement) => {
    resizeObserver?.disconnect();
    resizeObserver = new ResizeObserver(() => instance?.resize());
    resizeObserver.observe(el);
  };

  const render = () => {
    const el = container.value;
    const opt = toValue(option);
    if (!el || !opt) {
      instance?.dispose();
      instance = null;
      resizeObserver?.disconnect();
      resizeObserver = null;
      return;
    }
    if (!instance) {
      instance = echarts.init(el);
      bindResize(el);
    }
    instance.setOption(opt, true);
    instance.resize();
  };

  onMounted(() => {
    render();
    watch(() => toValue(option), render, { deep: true });
    watch(container, el => {
      if (!el) {
        instance?.dispose();
        instance = null;
        resizeObserver?.disconnect();
        resizeObserver = null;
        return;
      }
      render();
    });
  });

  onBeforeUnmount(() => {
    resizeObserver?.disconnect();
    instance?.dispose();
    instance = null;
  });
}
