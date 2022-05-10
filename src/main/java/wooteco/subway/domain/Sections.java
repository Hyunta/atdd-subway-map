package wooteco.subway.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Sections {
    private final List<Section> sections;

    public Sections(List<Section> sections) {
        this.sections = sections;
    }

    public void validateAddable(Section section) {
        checkNoStations(section);
        checkExist(section);
    }

    private void checkNoStations(Section section) {
        if (!allStationIds().contains(section.getUpStationId()) && !allStationIds().contains(
                section.getDownStationId())) {
            throw new IllegalArgumentException("적어도 한 개의 역이 노선 구간 내에 존재해야 합니다.");
        }
    }

    private void checkExist(Section section) {
        if (allStationIds().containsAll(List.of(section.getUpStationId(), section.getDownStationId()))) {
            throw new IllegalArgumentException("이미 등록된 구간입니다.");
        }
    }

    private List<Long> allStationIds() {
        List<Long> upStations = upStationIds();
        List<Long> downStations = downStationIds();
        return addListWithDistinct(upStations, downStations);
    }

    private List<Long> upStationIds() {
        return sections.stream()
                .map(Section::getUpStationId)
                .collect(Collectors.toList());
    }

    private List<Long> downStationIds() {
        return sections.stream()
                .map(Section::getDownStationId)
                .collect(Collectors.toList());
    }

    private ArrayList<Long> addListWithDistinct(List<Long> first, List<Long> second) {
        Set<Long> set = new LinkedHashSet<>(first);
        set.addAll(second);
        return new ArrayList<>(set);
    }

    public Section add(Section section) {
        if (matchUpStation(section)) {
            return sections.stream()
                    .filter(section::equalsUpStation)
                    .filter(section::isShorterThan)
                    .findAny()
                    .map(section::createDivideDownSection)
                    .orElseThrow(() -> new IllegalArgumentException("기존 구간보다 긴 역을 추가할 수 없습니다."));
        }

        if (matchDownStation(section)) {
            return sections.stream()
                    .filter(section::equalsDownStation)
                    .filter(section::isShorterThan)
                    .findAny()
                    .map(section::createDivideUpSection)
                    .orElseThrow(() -> new IllegalArgumentException("기존 구간보다 긴 역을 추가할 수 없습니다."));
        }
        for (Section each : sections) {
            if (each.getUpStationId().equals(section.getDownStationId())) {
                return section;
            }
        }

        for (Section each : sections) {
            if (each.getDownStationId().equals(section.getUpStationId())) {
                return section;
            }
        }

        throw new IllegalArgumentException("알 수 없는 오류로 추가할 수 없는 구간입니다.");
    }

    private boolean matchUpStation(Section section) {
        return sections.stream()
                .anyMatch(section::equalsUpStation);
    }

    private boolean matchDownStation(Section section) {
        return sections.stream()
                .anyMatch(section::equalsDownStation);
    }

}