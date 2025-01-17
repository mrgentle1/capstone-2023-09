package com.capstone.timepay.controller.board;


import com.capstone.timepay.controller.board.request.ReportRequestDTO;
import com.capstone.timepay.domain.dealBoard.DealBoard;
import com.capstone.timepay.service.board.dto.DealBoardDTO;
import com.capstone.timepay.service.board.service.DealBoardService;
import com.capstone.timepay.service.board.service.DealRegisterService;
import com.capstone.timepay.service.board.service.ReportService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/deal-boards")
public class DealBoardController
{
    private final DealBoardService dealBoardService;

    private final ReportService reportService;

    private final DealRegisterService dealRegisterService;



    @ApiOperation(value = "거래게시판 모든 게시판 불러오기")
    @GetMapping("")
    public ResponseEntity<Page<DealBoardDTO>> getBoards(
            @RequestParam(value = "pagingIndex", defaultValue = "0") int pagingIndex,
            @RequestParam(value = "pagingSize", defaultValue = "50") int pagingSize)
    {
        Page<DealBoardDTO> paging = dealBoardService.getDealBoards(pagingIndex, pagingSize);
        if (paging.isEmpty())
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(paging, HttpStatus.OK);
    }

    @ApiOperation(value = "도움주기 게시판 불러오기")
    @GetMapping("/helper")
    public ResponseEntity<Page<DealBoardDTO>> getHelperBoards(
            @RequestParam(value = "pagingIndex", defaultValue = "0") int pagingIndex,
            @RequestParam(value = "pagingSize", defaultValue = "50") int pagingSize)
    {
        Page<DealBoardDTO> paging = dealBoardService.getHelperDealBoard(pagingIndex, pagingSize);
        if (paging.isEmpty())
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(paging, HttpStatus.OK);
    }

    @ApiOperation(value = "도움요청 게시판 불러오기")
    @GetMapping("/help")
    public ResponseEntity<Page<DealBoardDTO>> getHelpBoards(
            @RequestParam(value = "pagingIndex", defaultValue = "0") int pagingIndex,
            @RequestParam(value = "pagingSize", defaultValue = "50") int pagingSize)
    {
        Page<DealBoardDTO> paging = dealBoardService.getHelpDealBoard(pagingIndex, pagingSize);
        if (paging.isEmpty())
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(paging, HttpStatus.OK);
    }

    @ApiOperation(value = "거래게시판 개별 게시판 불러오기")
    @GetMapping("/{id}")
    public ResponseEntity<DealBoardDTO> getBoard(@PathVariable("id") Long id)
    {
        return new ResponseEntity(dealBoardService.getDealBoard(id), HttpStatus.OK);
    }

    @ApiOperation(value = "거래게시판 도움주기 게시글 작성")
    @PostMapping(value = "/write/helper", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity helperWrite(@RequestPart DealBoardDTO dealBoardDTO,
                                      @RequestPart(required = false) List<MultipartFile> images,
                                      Principal principal) throws Exception
    {
        return new ResponseEntity(dealBoardService.helperWrite(dealBoardDTO, principal, "helper", images), HttpStatus.OK);
    }

    @ApiOperation(value = "거래게시판 도움요청 게시글 작성")
    @PostMapping(value = "/write/help", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity getHelpWrite(@RequestPart DealBoardDTO dealBoardDTO,
                                       @RequestPart(required = false) List<MultipartFile> images,
                                       Principal principal) throws Exception
    {
        return new ResponseEntity(dealBoardService.write(dealBoardDTO, principal, "help", images), HttpStatus.OK);
    }

    @ApiOperation(value = "거래게시판 게시글 수정")
    @PutMapping("/update/{id}")
    public Map<String, Object> update(@RequestBody DealBoardDTO dealBoardDTO,
                                      @PathVariable("id") Long id,
                                      Principal principal)
    {
        Map<String, Object> updateMap = new HashMap<>();
        DealBoard dealBoard = dealBoardService.getId(id);
        String boardEmail = dealRegisterService.getEmail(id);

        if (dealBoard == null)
        {
            updateMap.put("success", false);
            updateMap.put("message", "해당 게시글을 찾을 수 없습니다");
            return updateMap;
        }

        dealBoardService.update(id, dealBoardDTO);
        updateMap.put("success", true);
        updateMap.put("dealBoard", dealBoard);
        return updateMap;
    }

    @ApiOperation(value = "거래게시판 게시글 삭제")
    @DeleteMapping ("/delete/{id}")
    public Map<String, Object> delete(@PathVariable("id") Long id,
                                      Principal principal)
    {
        Map<String, Object> deleteMap = new HashMap<>();
        DealBoard dealBoard = dealBoardService.getId(id);
        String boardEmail = dealRegisterService.getEmail(id);

        if (dealBoard == null)
        {
            deleteMap.put("success", false);
            deleteMap.put("message", "해당 게시글을 찾을 수 없습니다");
            return deleteMap;
        }

        dealBoardService.delete(id);
        deleteMap.put("success", true);
        return deleteMap;
    }

    // == 글 작성 후 로직 == //
    @ApiOperation(value = "모집중에서 모집완료로 변경시키는 컨트롤러")
    @PutMapping("/{boardId}/start")
    public Map<String, Object> readyToStart(@PathVariable("boardId") Long boardId,
                                            Principal principal)
    {
        Map<String, Object> resultMap = new HashMap<>();
        DealBoard dealBoard  = dealBoardService.getId(boardId);

        if (dealBoard == null)
        {
            resultMap.put("success", false);
            resultMap.put("message", "해당 게시글을 찾을 수 없습니다");
            return resultMap;
        }

        dealBoardService.modifyMatchingFinish(boardId);
        resultMap.put("success", true);
        return resultMap;
    }

    @ApiOperation(value = "활동완료로로 변경시키는 컨트롤러")
    @PutMapping("/{boardId}/finish")
    public Map<String, Object> activityFinish(@PathVariable("boardId") Long boardId,
                                              Principal principal)
    {
        Map<String, Object> resultMap = new HashMap<>();
        DealBoard dealBoard  = dealBoardService.getId(boardId);

        if (dealBoard == null)
        {
            resultMap.put("success", false);
            resultMap.put("message", "해당 게시글을 찾을 수 없습니다");
            return resultMap;
        }

        dealBoardService.modifyActivityFinish(boardId);
        resultMap.put("success", true);
        return resultMap;
    }

    @PostMapping("/{boardId}/report")
    @ApiOperation(value = "신고 API", notes = "JWT 토큰으로 유저를 구분하여 신고 DB에 작성합니다.")
    public ResponseEntity<?> report(@PathVariable("boardId") Long boardId, @RequestBody ReportRequestDTO requestDTO) {
        /* 현재 인증된 사용자의 인증 토큰을 가져온다.*/
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(reportService.reportBoard(authentication, boardId, requestDTO, "거래신고"));
    }
}

