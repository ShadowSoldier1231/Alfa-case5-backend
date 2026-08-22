package com.project.main.enums;

public enum Achievement {
    QUICK_START(1L, "Быстрый старт", "Решите первый кейс менее чем за 30 минут", "/achievements/quick_start.png"),
    RAPID_RISE(2L, "Стремительный взлёт", "Решите 5 кейсов", "/achievements/rapid_rise.png"),
    COLLECTOR(3L, "Коллекционер", "Решите 20 кейсов", "/achievements/collector.png"),
    TAG_MASTER(4L, "Мастер на все теги", "Решите кейсы из 5 разных тегов", "/achievements/tag_master.png"),
    DATA_MASTER(5L, "Data-мастер", "Решите 5 кейсов с тегом «Data Science»", "/achievements/data_master.png"),
    BUSINESS_LEADER(6L, "Бизнес-лидер", "Решите 5 кейсов с тегом «Бизнес-стратегия»", "/achievements/business_leader.png"),
    PERFECT_SOLUTION(7L, "Идеальное решение", "Получите 100% баллов за любой кейс", "/achievements/perfect_solution.png"),
    FIRST_TRY(8L, "С первой попытки", "Сдайте кейс на 100% с первой попытки", "/achievements/first_try.png"),
    HARDCORE_SOLVER(9L, "Хардкорщик", "Решите 3 кейса высокого уровня сложности", "/achievements/hardcore_solver.png"),
    SPRINTER(10L, "Спринтер", "Решите кейс быстрее среднего времени выполнения", "/achievements/sprinter.png"),
    MARATHONER(11L, "Марафонец", "Решите 3 кейса без пропусков дней (1 кейс - 1 день)", "/achievements/marathoner.png"),
    SCOUNDREL(12L, "Поганец", "Получи 1 предупреждение от бота", "/achievements/scoundrel.png"),
    PERFECTIONIST(13L, "Перфекционист", "Получите максимальный балл за 3 разных кейса", "/achievements/perfectionist.png"),
    OPINION_LEADER(14L, "Лидер мнений", "Попадите в топ-3 рейтинга", "/achievements/opinion_leader.png");


    private final Long id;
    private final String name;
    private final String description;
    private final String iconUrl;

    Achievement(Long id, String name, String description, String iconUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}

