import { Component, computed, input } from '@angular/core';

export interface BarChartItem {
  label: string;
  value: number;
}

@Component({
  selector: 'app-bar-chart',
  imports: [],
  templateUrl: './bar-chart.html',
  styleUrl: './bar-chart.css',
})
export class BarChart {
  readonly items = input<BarChartItem[]>([]);

  readonly maxValue = computed(() => Math.max(1, ...this.items().map((item) => item.value)));

  readonly rows = computed(() =>
    this.items().map((item) => ({
      ...item,
      widthPct: (item.value / this.maxValue()) * 100,
    })),
  );
}
