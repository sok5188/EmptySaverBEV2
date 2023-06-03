package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.CategoryDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberInterest;
import com.example.emptySaver.domain.entity.category.*;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.repository.MemberInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MemberInterestRepository memberInterestRepository;
    private final MemberService memberService;
    private static CategoryDto.res allCategory;
    private static final Map<String,String> labelMap=new HashMap<>();
    private static final Map<String,List<String>> categoryMap=new HashMap<>();
    private static final Map<String,String> categoryNameMap=new HashMap<>();
    @PostConstruct
    public void setAllCategories() {
        List<CategoryDto.fullCategoryInfo> typeList=new ArrayList<>();
        List<String> playCollect = Arrays.stream(PlayType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("play",playCollect);
        typeList.add(new CategoryDto.fullCategoryInfo<>("play","오락", playCollect));
        labelMap.putAll(Arrays.stream(PlayType.values()).collect(Collectors.toMap(PlayType::getLabel, PlayType::getKey)));
        categoryNameMap.put("play","오락");

        List<String> movieCollect = Arrays.stream(MovieType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("movie",movieCollect);
        typeList.add(new CategoryDto.fullCategoryInfo<>("movie","영화",movieCollect));
        labelMap.putAll(Arrays.stream(MovieType.values()).collect(Collectors.toMap(MovieType::getLabel,MovieType::getKey)));
        categoryNameMap.put("movie","영화");

        List<String> sportsCollect = Arrays.stream(SportsType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("sports",sportsCollect);
        typeList.add(new CategoryDto.fullCategoryInfo<>("sports","스포츠", sportsCollect));
        labelMap.putAll(Arrays.stream(SportsType.values()).collect(Collectors.toMap(SportsType::getLabel,SportsType::getKey)));
        categoryNameMap.put("sports","스포츠");

        List<String> studyCollect = Arrays.stream(StudyType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("study",studyCollect);
        typeList.add(new CategoryDto.fullCategoryInfo<>("study","스터디", studyCollect));
        labelMap.putAll(Arrays.stream(StudyType.values()).collect(Collectors.toMap(StudyType::getLabel,StudyType::getKey)));
        categoryNameMap.put("study","스터디");

        List<String> freeCollect=new ArrayList<>();
        freeCollect.add("자율");
        categoryMap.put("free",freeCollect);
        typeList.add(new CategoryDto.fullCategoryInfo<>("free","자율",freeCollect));
        labelMap.putAll(Arrays.stream(FreeType.values()).collect(Collectors.toMap(FreeType::getLabel, FreeType::getKey)));
        categoryNameMap.put("free","자율");

        allCategory=new CategoryDto.res(typeList);

    }
    public Integer getTotalLabelCount(){
        return labelMap.size();
    }
    public Integer getTargetCategoryLabelCount(String categoryName){ return categoryMap.get(categoryName).size();}

    public CategoryDto.res getAllCategories() {
        return allCategory;
    }
    public Category getCategoryByLabel(String label){
        if(!labelMap.containsKey(label))
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);

        List<Play> allPlay = this.findAllPlay();
        Optional<Play> playOptional = allPlay.stream().filter(g -> g.getPlayType().getLabel().equals(label)).findAny();
        if(playOptional.isPresent())
            return playOptional.get();

        List<Sports> allSports = this.findAllSports();
        Optional<Sports> sportsOptional = allSports.stream().filter(g -> g.getSportType().getLabel().equals(label)).findAny();
        if(sportsOptional.isPresent())
            return sportsOptional.get();

        List<Movie> allMovie = this.findAllMovie();
        Optional<Movie> movieOptional = allMovie.stream().filter(m -> m.getMovieGenre().getLabel().equals(label)).findAny();
        if(movieOptional.isPresent())
            return movieOptional.get();

        List<Study> allStudy = this.findAllStudy();
        Optional<Study> studyOptional = allStudy.stream().filter(m -> m.getStudyType().getLabel().equals(label)).findAny();
        if(studyOptional.isPresent())
            return studyOptional.get();

        List<Free> allFree = this.findAllFree();
        Optional<Free> etcOptional = allFree.stream().filter(e -> e.getFreeType().getLabel().equals(label)).findAny();
        if(etcOptional.isPresent())
            return etcOptional.get();

        throw new BaseException(BaseResponseStatus.INVALID_LABEL_NAME);
    }

    public String getLabelByCategory(Category category){
        log.info("now get label by category, id and class : "+category.getId()+ " / "+ category.getClass());

        if(category instanceof Play){
            Play play = (Play) category;
            return play.getPlayType().getLabel();
        }else if(category instanceof Movie){
            Movie movie = (Movie) category;
            return movie.getMovieGenre().getLabel();
        }else if(category instanceof Sports){
            Sports sports= (Sports) category;
            return sports.getSportType().getLabel();
        }else if(category instanceof Study){
            Study study= (Study) category;
            return study.getStudyType().getLabel();
        }else if(category instanceof Free){
            Free free =(Free) category;
            return free.getFreeType().getLabel();
        }
        else{
            throw new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID);
        }
    }
    public String getCategoryNameByCategory(Category category){
        log.info("category id: "  + category.getId());
        if(category instanceof Play){
            return "오락";
        }else if(category instanceof Movie){
            return "영화";
        }else if(category instanceof Sports){
            return "스포츠";
        }else if(category instanceof Study){
            return "스터디";
        }else if(category instanceof Free){
            return "자율";
        }
        else{
            throw new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID);
        }
    }
    public List<? extends Category> getCategoryByName(String name){

        if(name.equals("play")){
            return this.findAllPlay();
        }else if(name.equals("movie"))
            return this.findAllMovie();
        else if(name.equals("sports"))
            return this.findAllSports();
        else if(name.equals("study"))
            return this.findAllStudy();
        else if(name.equals("free"))
            return this.findAllFree();
        else throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
    }
    public List<CategoryDto.categoryInfo> getCategoryNames(){
        List<CategoryDto.categoryInfo> result = new ArrayList<>();
        categoryNameMap.forEach((s, s2) -> result.add(new CategoryDto.categoryInfo(s,s2)));
        return result;
    }
    public CategoryDto.categoryType getLabelInfoByCategoryName(String type){
        if(!categoryMap.containsKey(type))
            throw new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID);
        List<String> labelList = categoryMap.get(type);
        return new CategoryDto.categoryType(type,labelList);
    }
    public List<Play> findAllPlay(){
        return categoryRepository.findAllPlay();
    }
    public List<Movie> findAllMovie(){
        return categoryRepository.findAllMovie();
    }
    public List<Sports> findAllSports(){
        return categoryRepository.findAllSports();
    }
    public List<Study> findAllStudy(){
        return categoryRepository.findAllStudy();
    }
    public List<Free> findAllFree(){
        return categoryRepository.findAllFree();
    }
    public Optional<? extends Category> getListByCategoryAndLabel(String categoryName, String labelName){

        if(categoryName.equals("play")){
            return this.findAllPlay().stream().filter(g->g.getPlayType().getLabel().equals(labelName)).findAny();
        }else if(categoryName.equals("movie")){
            return this.findAllMovie().stream().filter(m->m.getMovieGenre().getLabel().equals(labelName)).findAny();
        }
        else if(categoryName.equals("sports"))
            return this.findAllSports().stream().filter(sports -> sports.getSportType().getLabel().equals(labelName)).findAny();
        else if(categoryName.equals("study"))
            return this.findAllStudy().stream().filter(study -> study.getStudyType().getLabel().equals(labelName)).findAny();
        else if(categoryName.equals("free"))
            return this.findAllFree().stream().filter(f->f.getFreeType().getLabel().equals(labelName)).findAny();
        else throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
    }
    @Transactional
    public void saveInterest(CategoryDto.saveMemberInterestReq req){
        List<MemberInterest> memberInterestList = new ArrayList<>();
        Member member = memberService.getMemberByEmail(req.getEmail());
        List<CategoryDto.memberInterestForm> formList = req.getFormList();
        formList.stream().forEach(form-> form.getTagList()
                .forEach(tag->memberInterestList
                        .add(MemberInterest.init().member(member)
                                .category(this.getListByCategoryAndLabel(form.getType(),tag)
                                        .orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID))).build())
                        ));
        memberInterestRepository.saveAll(memberInterestList);
    }

}
