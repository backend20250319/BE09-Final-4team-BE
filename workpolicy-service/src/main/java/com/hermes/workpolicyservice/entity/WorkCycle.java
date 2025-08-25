package com.hermes.workpolicyservice.entity;

/**
 * 근무 주기를 나타내는 enum
 * 선택 근무 전용이며, 다른 근무 유형의 경우 1주가 기본값으로 사용됨
 */
public enum WorkCycle {
    ONE_WEEK("1주", 1),
    TWO_WEEKS("2주", 2),
    THREE_WEEKS("3주", 3),
    FOUR_WEEKS("4주", 4),
    ONE_MONTH("1개월", 4);  // 1개월은 현재 달 정보 가져오기
    
    private final String name;
    private final int weekCount;
    
    WorkCycle(String name, int weekCount) {
        this.name = name;
        this.weekCount = weekCount;
    }
    
    public String getName() {
        return name;
    }
    
    public int getWeekCount() {
        return weekCount;
    }
    
    /**
     * 선택 근무가 아닌 경우 기본값인 1주를 반환
     * @param workType 근무 유형
     * @return 적절한 근무 주기
     */
    public static WorkCycle getDefaultForWorkType(WorkType workType) {
        if (workType == WorkType.OPTIONAL) {
            return ONE_WEEK; // 선택 근무의 기본값
        }
        return ONE_WEEK; // 선택 근무가 아닌 경우 1주가 기본값
    }
    
    /**
     * 선택 근무 전용 주기인지 확인
     * @param workType 근무 유형
     * @return 선택 근무인 경우 true, 아니면 false
     */
    public static boolean isApplicableForWorkType(WorkType workType) {
        return workType == WorkType.OPTIONAL;
    }
}
