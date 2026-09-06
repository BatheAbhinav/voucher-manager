import { Component, computed, input, signal } from '@angular/core';
import { DailyRedemptionCount } from '../stats.model';

@Component({
  selector: 'app-redemption-trend-chart',
  imports: [],
  templateUrl: './redemption-trend-chart.html',
  styleUrl: './redemption-trend-chart.css',
})
export class RedemptionTrendChart {
  readonly data = input<DailyRedemptionCount[]>([]);

  readonly width = 720;
  readonly height = 220;
  private readonly padTop = 16;
  private readonly padRight = 16;
  private readonly padBottom = 28;
  private readonly padLeft = 36;

  readonly innerWidth = this.width - this.padLeft - this.padRight;
  readonly innerHeight = this.height - this.padTop - this.padBottom;
  readonly baselineY = this.padTop + this.innerHeight;

  readonly hoverIndex = signal<number | null>(null);

  readonly maxValue = computed(() => Math.max(1, ...this.data().map((d) => d.count)));

  readonly points = computed(() => {
    const rows = this.data();
    const max = this.maxValue();
    const step = rows.length > 1 ? this.innerWidth / (rows.length - 1) : 0;
    return rows.map((row, i) => ({
      x: this.padLeft + i * step,
      y: this.padTop + this.innerHeight - (row.count / max) * this.innerHeight,
      day: row.day,
      count: row.count,
    }));
  });

  readonly linePath = computed(() =>
    this.points()
      .map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`)
      .join(' '),
  );

  readonly areaPath = computed(() => {
    const pts = this.points();
    if (pts.length === 0) {
      return '';
    }
    const first = pts[0];
    const last = pts[pts.length - 1];
    return `${this.linePath()} L ${last.x.toFixed(1)} ${this.baselineY} L ${first.x.toFixed(1)} ${this.baselineY} Z`;
  });

  readonly gridlineValues = computed(() => {
    const max = this.maxValue();
    return [0, Math.round(max / 2), max];
  });

  readonly hovered = computed(() => {
    const index = this.hoverIndex();
    const pts = this.points();
    return index !== null ? pts[index] : null;
  });

  gridlineY(value: number): number {
    return this.padTop + this.innerHeight - (value / this.maxValue()) * this.innerHeight;
  }

  onMouseMove(event: MouseEvent, svg: Element): void {
    const pts = this.points();
    if (pts.length === 0) {
      return;
    }
    const rect = svg.getBoundingClientRect();
    const scaleX = this.width / rect.width;
    const x = (event.clientX - rect.left) * scaleX;

    let nearest = 0;
    let nearestDist = Infinity;
    pts.forEach((p, i) => {
      const dist = Math.abs(p.x - x);
      if (dist < nearestDist) {
        nearestDist = dist;
        nearest = i;
      }
    });
    this.hoverIndex.set(nearest);
  }

  onMouseLeave(): void {
    this.hoverIndex.set(null);
  }

  formatDay(day: string): string {
    return new Date(day).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  }
}
