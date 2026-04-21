<script setup lang="ts">
/**
 * 友链插画场景组件。
 * 作用：为友链页和友链申请页提供统一的治愈系手绘动画感场景，营造更轻盈的页面情绪。
 */
const props = withDefaults(defineProps<{
  compact?: boolean
}>(), {
  compact: false
})

/**
 * 计算场景容器高度。
 * 作用：根据是否紧凑模式切换插画区高度，兼顾列表页与申请页两种版面。
 */
const sceneHeightClass = computed(() => {
  return props.compact ? 'h-[18rem] sm:h-[20rem]' : 'h-[24rem] sm:h-[28rem] lg:h-[32rem]'
})
</script>

<template>
  <div
    :class="[
      'friend-link-scene relative overflow-hidden rounded-[32px] border border-white/70 bg-[linear-gradient(180deg,#f8fafc_0%,#eef2ff_54%,#f8fafc_100%)] shadow-[0_26px_64px_-46px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-[linear-gradient(180deg,#020617_0%,#0f172a_56%,#111827_100%)]',
      sceneHeightClass
    ]"
  >
    <div class="scene-grid" />
    <div class="cloud cloud-a" />
    <div class="cloud cloud-b" />

    <div class="sun-halo" />

    <div class="mountain mountain-back" />
    <div class="mountain mountain-mid" />
    <div class="hill hill-left" />
    <div class="hill hill-right" />

    <div class="house">
      <div class="house-roof" />
      <div class="house-body">
        <div class="house-window" />
        <div class="house-door" />
      </div>
    </div>

    <div class="paper-bird" />

    <div class="meadow" />
  </div>
</template>

<style scoped>
.friend-link-scene::after {
  content: '';
  position: absolute;
  inset: auto 0 0 0;
  height: 38%;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.14), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(248, 250, 252, 0.2));
  pointer-events: none;
}

.scene-grid {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 30px 30px;
  mask-image: linear-gradient(180deg, rgba(255, 255, 255, 0.52), transparent 82%);
  pointer-events: none;
}

.dark .scene-grid {
  background:
    linear-gradient(90deg, rgba(71, 85, 105, 0.22) 1px, transparent 1px),
    linear-gradient(180deg, rgba(71, 85, 105, 0.22) 1px, transparent 1px);
}

.cloud,
.sun-halo,
.mountain,
.hill,
.house,
.paper-bird,
.meadow {
  position: absolute;
}

.cloud {
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  filter: blur(0.2px);
  box-shadow:
    2rem 0 rgba(255, 255, 255, 0.62),
    4.1rem 0.35rem rgba(255, 255, 255, 0.56),
    1rem -0.7rem rgba(255, 255, 255, 0.68);
  animation: cloud-drift 22s ease-in-out infinite;
}

.dark .cloud {
  background: rgba(255, 255, 255, 0.1);
  box-shadow:
    2rem 0 rgba(255, 255, 255, 0.1),
    4.1rem 0.35rem rgba(255, 255, 255, 0.08),
    1rem -0.7rem rgba(255, 255, 255, 0.14);
}

.cloud-a {
  top: 14%;
  left: 8%;
  width: 5rem;
  height: 1.5rem;
}

.cloud-b {
  top: 18%;
  right: 16%;
  width: 4.2rem;
  height: 1.3rem;
  animation-duration: 26s;
}

.sun-halo {
  top: 8%;
  right: 10%;
  width: 8.5rem;
  height: 8.5rem;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(191, 219, 254, 0.36), rgba(191, 219, 254, 0));
  filter: blur(10px);
  animation: pulse-sun 12s ease-in-out infinite;
}

.dark .sun-halo {
  background: radial-gradient(circle, rgba(56, 189, 248, 0.14), rgba(56, 189, 248, 0));
}

.mountain-back {
  left: -4%;
  bottom: 34%;
  width: 56%;
  height: 28%;
  background: linear-gradient(180deg, rgba(203, 213, 225, 0.72), rgba(148, 163, 184, 0.9));
  clip-path: polygon(0% 100%, 18% 52%, 32% 63%, 48% 34%, 70% 68%, 86% 46%, 100% 100%);
}

.mountain-mid {
  right: -6%;
  bottom: 30%;
  width: 60%;
  height: 31%;
  background: linear-gradient(180deg, rgba(186, 230, 253, 0.76), rgba(125, 211, 252, 0.88));
  clip-path: polygon(0% 100%, 12% 58%, 24% 70%, 42% 36%, 58% 60%, 76% 42%, 100% 100%);
}

.dark .mountain-back {
  background: linear-gradient(180deg, rgba(51, 65, 85, 0.78), rgba(30, 41, 59, 0.96));
}

.dark .mountain-mid {
  background: linear-gradient(180deg, rgba(30, 64, 175, 0.24), rgba(15, 23, 42, 0.98));
}

.hill {
  bottom: 16%;
  height: 28%;
  background: linear-gradient(180deg, rgba(186, 230, 253, 0.82), rgba(148, 163, 184, 0.94));
}

.dark .hill {
  background: linear-gradient(180deg, rgba(22, 78, 99, 0.54), rgba(15, 23, 42, 0.96));
}

.hill-left {
  left: -3%;
  width: 64%;
  clip-path: ellipse(56% 66% at 38% 78%);
}

.hill-right {
  right: -6%;
  width: 62%;
  clip-path: ellipse(60% 68% at 58% 78%);
}

.house {
  left: 28%;
  bottom: 22%;
  width: 7.8rem;
  animation: float-soft 9s ease-in-out infinite;
}

.house-roof {
  position: absolute;
  left: 0.7rem;
  top: 0;
  width: 6.4rem;
  height: 2.3rem;
  background: linear-gradient(180deg, #d6a36b, #bf8451);
  clip-path: polygon(6% 100%, 50% 0%, 94% 100%);
}

.house-body {
  position: absolute;
  left: 1.15rem;
  top: 1.8rem;
  width: 5.4rem;
  height: 4rem;
  border-radius: 1rem 1rem 0.8rem 0.8rem;
  background: linear-gradient(180deg, #f8f1df, #ece2c9);
  box-shadow: inset 0 -0.3rem rgba(148, 109, 61, 0.1);
}

.house-window {
  position: absolute;
  left: 0.8rem;
  top: 0.9rem;
  width: 1.2rem;
  height: 1.2rem;
  border-radius: 0.35rem;
  background: linear-gradient(180deg, #ffffff, #dbeafe);
}

.house-door {
  position: absolute;
  right: 0.85rem;
  bottom: 0;
  width: 1.1rem;
  height: 1.8rem;
  border-radius: 0.6rem 0.6rem 0 0;
  background: linear-gradient(180deg, #94a3b8, #64748b);
}

.paper-bird {
  top: 28%;
  left: 60%;
  width: 2.4rem;
  height: 1.2rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.12));
  clip-path: polygon(0 58%, 48% 0, 100% 58%, 62% 54%, 52% 100%, 40% 54%);
  animation: bird-glide 9s ease-in-out infinite;
}

.meadow {
  inset: auto 0 0 0;
  height: 22%;
  background:
    radial-gradient(circle at 12% 22%, rgba(255, 255, 255, 0.16), transparent 16%),
    linear-gradient(180deg, rgba(226, 232, 240, 0.9), rgba(203, 213, 225, 0.98));
}

.dark .meadow {
  background:
    radial-gradient(circle at 12% 22%, rgba(255, 255, 255, 0.05), transparent 16%),
    linear-gradient(180deg, rgba(30, 41, 59, 0.96), rgba(15, 23, 42, 1));
}

@keyframes cloud-drift {
  0%, 100% {
    transform: translate3d(0, 0, 0);
  }
  50% {
    transform: translate3d(1rem, -0.3rem, 0);
  }
}

@keyframes pulse-sun {
  0%, 100% {
    transform: scale(0.96);
    opacity: 0.54;
  }
  50% {
    transform: scale(1.03);
    opacity: 0.76;
  }
}

@keyframes float-soft {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-0.35rem);
  }
}

@keyframes bird-glide {
  0%, 100% {
    transform: translate3d(0, 0, 0) rotate(4deg);
  }
  50% {
    transform: translate3d(1rem, -0.5rem, 0) rotate(-2deg);
  }
}
</style>
