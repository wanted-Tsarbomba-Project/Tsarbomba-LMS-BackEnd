function safeName(name) {
    return String(name || "k6-result")
        .replace(/[^a-zA-Z0-9._-]/g, "-")
        .replace(/-+/g, "-");
}

function metricValue(data, metricName, key) {
    const metric = data.metrics[metricName];
    if (!metric || !metric.values || metric.values[key] === undefined) {
        return "";
    }

    const value = metric.values[key];
    if (typeof value !== "number") {
        return String(value);
    }

    return Number.isInteger(value) ? String(value) : value.toFixed(2);
}

function rateValue(data, metricName) {
    const metric = data.metrics[metricName];
    if (!metric || !metric.values || metric.values.rate === undefined) {
        return "";
    }

    return `${(metric.values.rate * 100).toFixed(2)}%`;
}

function countValue(data, metricName) {
    return metricValue(data, metricName, "count");
}

function durationRow(data, metricName) {
    return `| ${metricName} | ${metricValue(data, metricName, "avg")} | ${metricValue(data, metricName, "min")} | ${metricValue(data, metricName, "med")} | ${metricValue(data, metricName, "p(90)")} | ${metricValue(data, metricName, "p(95)")} | ${metricValue(data, metricName, "p(99)")} | ${metricValue(data, metricName, "max")} |`;
}

function collectChecks(group, out = []) {
    for (const check of group.checks || []) {
        out.push(check);
    }
    for (const childGroup of group.groups || []) {
        collectChecks(childGroup, out);
    }
    return out;
}

function buildMarkdown(data, name) {
    const lines = [];

    lines.push(`# k6 Result - ${name}`);
    lines.push("");
    lines.push("## Summary");
    lines.push("");
    lines.push("| Metric | Value |");
    lines.push("| --- | ---: |");
    lines.push(`| http_reqs | ${countValue(data, "http_reqs")} |`);
    lines.push(`| iterations | ${countValue(data, "iterations")} |`);
    lines.push(`| checks success rate | ${rateValue(data, "checks")} |`);
    lines.push(`| http_req_failed | ${rateValue(data, "http_req_failed")} |`);
    lines.push(`| data_received bytes | ${countValue(data, "data_received")} |`);
    lines.push(`| data_sent bytes | ${countValue(data, "data_sent")} |`);
    lines.push("");
    lines.push("## Duration Metrics");
    lines.push("");
    lines.push("| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |");
    lines.push("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |");
    lines.push(durationRow(data, "http_req_duration"));
    lines.push(durationRow(data, "http_req_waiting"));
    lines.push(durationRow(data, "http_req_blocked"));
    lines.push(durationRow(data, "http_req_connecting"));
    lines.push("");
    lines.push("## Checks");
    lines.push("");
    lines.push("| Check | Result |");
    lines.push("| --- | --- |");

    const checks = collectChecks(data.root_group);
    if (checks.length === 0) {
        lines.push("| check details | Check console output or JSON summary. |");
    } else {
        for (const item of checks) {
            lines.push(`| ${item.name} | ${item.passes} pass / ${item.fails} fail |`);
        }
    }
    lines.push("");

    return `${lines.join("\n")}\n`;
}

function buildConsoleTable(data, name) {
    return [
        "",
        `k6 summary: ${name}`,
        "------------------------------------------------------------",
        `http_reqs        : ${countValue(data, "http_reqs")}`,
        `iterations       : ${countValue(data, "iterations")}`,
        `checks           : ${rateValue(data, "checks")}`,
        `http_req_failed  : ${rateValue(data, "http_req_failed")}`,
        "",
        "http_req_duration (ms)",
        `  avg             : ${metricValue(data, "http_req_duration", "avg")}`,
        `  med             : ${metricValue(data, "http_req_duration", "med")}`,
        `  p90             : ${metricValue(data, "http_req_duration", "p(90)")}`,
        `  p95             : ${metricValue(data, "http_req_duration", "p(95)")}`,
        `  p99             : ${metricValue(data, "http_req_duration", "p(99)")}`,
        `  max             : ${metricValue(data, "http_req_duration", "max")}`,
        "------------------------------------------------------------",
        `Markdown report  : monitoring/k6/results/${name}-summary.md`,
        `JSON report      : monitoring/k6/results/${name}-summary.json`,
        "",
    ].join("\n");
}

export function createSummaryHandler(defaultName) {
    return function handleSummary(data) {
        const name = safeName(__ENV.RESULT_NAME || defaultName);

        return {
            stdout: buildConsoleTable(data, name),
            [`/results/${name}-summary.md`]: buildMarkdown(data, name),
            [`/results/${name}-summary.json`]: JSON.stringify(data, null, 2),
        };
    };
}
