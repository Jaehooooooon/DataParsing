#  좌석 혼잡도 기반 경로 추천 서비스

## 소개
**좌석 혼잡도 기반 경로 추천 서비스**는 기존의 최단시간 / 최단거리 기반의 경로 추천에서 벗어나 소요시간이 좀 더 길어도 앉아서 이동하고자 하는 사람들을 대상으로 버스 내의 혼잡도가 최저인 길을 안내하고자 하는 서비스이다. 몸이 불편하거나 이동이 불편한 교통약자, 혹은 짐이 많거나 피곤에 지친 사람들을 서비스의 주요 대상으로 한다.

## 필요성
우리나라 전체 인구의 약 33%가 이동제약자에 해당하는 것으로 확인되었다. (출처: 2018 전국 교통약자 현황 통계자료) 비록 이동에 어려움이 있는 교통약자라도 대중교통 이용은 불가피하다. 이에 따라 다양한 서비스가 실행되고 있지만, 대중교통 내부의 혼잡도를 기반으로 경로를 추천해주기까지 하는 서비스는 현존하지 않으므로 본 서비스의 필요성이 부각된다. 해당 서비스는 간단한 UI와 함께 안드로이드 앱으로 구현하였으며 핵심 기능은 따로 모듈로 제공하여 기존의 길찾기API와 같은 방법으로 사용이 가능하도록 할 예정이다.

-------------------------

## 구현 범위
- 서울 시내 버스만 고려 (지하철은 고려하지 않음)
- 서울 시내 출발지와 도착지 지정
- 버스 정류장 사이의 시간간격은 데이터 부족으로 인해 정확한 값을 얻어올 수 없으므로 버스 종류별로 아래와 같이 평균적인 값으로 고정한다.
    |버스|시간(분)|
    |---|---|
    |광역버스|15|
    |지선버스|4|
    |간선버스|5|
    |순환버스|5|
    |마을버스|3|
    |심야버스|15|


    ## 사용 데이터
    - 서울시 버스 정류장/노선별 승하차 인원정보 (perYear(2015-2018), perYear(2015-2018))
    - 서울시 노선현황정보 (2019년 1월 24일 기준)
    - 서울시 버스노선정보 (2019년 1월 24일 기준)


    ## 사용 API
    * [공공데이터포털](data.go.kr)
      - 버스 위치정보 조회 서비스
      - 버스 도착정보 조회 서비스(좌표기반 근접 정류소)
      - 대중교통환승경로 조회 서비스
    * [카카오 API](https://developers.kakao.com/docs/restapi/local)
      - 주소검색 API
    * [다음 API](http://apis.map.daum.net/)
      - Daum 지도 API

------------------------------
## 시작하기
#### 실행
 해당 서비스를 안드로이드 앱으로 구현하였으므로 앱을 설치 후 실행하면 된다. (앱은 출시 예정)
#### 사용법
1. 출발지와 도착지를 키워드 입력  후 검색을 눌러 정확한 주소명을 선택한다.
2. 이동 초기의 혼잡도를 우선적으로 고려한 여러 길찾기가 리스트뷰로 띄워진다.
3. 리스트에서 이용하고자 하는 길찾기 항목을 선택한다.
4. 사용자는 해당 경로로 이동 중 실시간으로 혼잡도가 낮은 새로운 환승 경로를 추천받을 수 있다.
5. 기존 경로가 아니라 추천받은 경로로 이동하고자 할 경우 해당 항목을 선택한다.

## Contribution Guidelines
본 프로젝트에 Contribution을 하고자 할 경우 [CONTRIBUTING.md](https://github.com/CAU-OSS-2019/team-project-team5/blob/master/CONTRIBUTING.md)을 읽고 언급된 순서로 진행한다. 또한  [CODE_OF_CONDUCT.md](https://github.com/CAU-OSS-2019/team-project-team5/blob/master/CODE_OF_CONDUCT.md)을 반드시 숙지한다.

## License
GNU General Public License version 3.0 (GPLv3)
