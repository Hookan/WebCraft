/*

                  GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]

 FULL LICENSE SEE https://github.com/ultralight-ux/AppCore/blob/master/LICENSE
*/
#pragma once
#include <Ultralight/platform/GPUDriver.h>
#include "../glad/glad.h"
#include <vector>
#include <map>

extern "C" {

void set_glfwGetTime_address(void* address);

}

namespace ultralight {

typedef ShaderType ProgramType;

class GPUDriverGL : public GPUDriver {
public:
  GPUDriverGL();

  virtual ~GPUDriverGL() { }

#if ENABLE_OFFSCREEN_GL
  virtual void SetRenderBufferBitmap(uint32_t render_buffer_id,
    RefPtr<Bitmap> bitmap);

  virtual bool IsRenderBufferBitmapDirty(uint32_t render_buffer_id);

  virtual void SetRenderBufferBitmapDirty(uint32_t render_buffer_id,
    bool dirty);
#endif

  virtual void BeginSynchronize() override { }

  virtual void EndSynchronize() override { }

  virtual uint32_t NextTextureId() override { return next_texture_id_++; }

  virtual void CreateTexture(uint32_t texture_id,
    Ref<Bitmap> bitmap) override;

  virtual void UpdateTexture(uint32_t texture_id,
    Ref<Bitmap> bitmap) override;

  virtual void BindTexture(uint8_t texture_unit,
    uint32_t texture_id) override;

  virtual void DestroyTexture(uint32_t texture_id) override;

  virtual uint32_t NextRenderBufferId() override { return next_render_buffer_id_++; }

  virtual void CreateRenderBuffer(uint32_t render_buffer_id,
    const RenderBuffer& buffer) override;

  virtual void BindRenderBuffer(uint32_t render_buffer_id) override;

  virtual void ClearRenderBuffer(uint32_t render_buffer_id) override;

  virtual void DestroyRenderBuffer(uint32_t render_buffer_id) override;

  virtual uint32_t NextGeometryId() override { return next_geometry_id_++; }

  virtual void CreateGeometry(uint32_t geometry_id,
    const VertexBuffer& vertices,
    const IndexBuffer& indices) override;

  virtual void UpdateGeometry(uint32_t geometry_id,
    const VertexBuffer& vertices,
    const IndexBuffer& indices) override;

  virtual void DrawGeometry(uint32_t geometry_id,
    uint32_t indices_count,
    uint32_t indices_offset,
    const GPUState& state) override;

  virtual void DestroyGeometry(uint32_t geometry_id) override;

  virtual void UpdateCommandList(const CommandList& list) override;

  virtual bool HasCommandsPending() override { return !command_list.empty(); }

  virtual void DrawCommandList() override;

  int batch_count() { return batch_count_; }

  void BindUltralightTexture(uint32_t ultralight_texture_id);

  void LoadPrograms();
  void DestroyPrograms();

  void LoadProgram(ProgramType type);
  void SelectProgram(ProgramType type);
  void UpdateUniforms(const GPUState& state);
  void SetUniform1ui(const char* name, GLuint val);
  void SetUniform1f(const char* name, float val);
  void SetUniform1fv(const char* name, size_t count, const float* val);
  void SetUniform4f(const char* name, const float val[4]);
  void SetUniform4fv(const char* name, size_t count, const float* val);
  void SetUniformMatrix4fv(const char* name, size_t count, const float* val);
  void SetViewport(float width, float height);

  int GetOpenGLTextureByUT(int );

protected:
  Matrix ApplyProjection(const Matrix4x4& transform, float screen_width, float screen_height, bool flip_y);

  void CreateFBOTexture(uint32_t texture_id, Ref<Bitmap> bitmap);

  struct TextureEntry {
    GLuint tex_id = 0; // GL Texture ID
    GLuint msaa_tex_id = 0; // GL Texture ID (only used if MSAA is enabled)
    uint32_t render_buffer_id = 0; // Used to check if we need to perform MSAA resolve
    GLuint width, height; // Used when resolving MSAA FBO, only valid if FBO
    bool is_sRGB = false; // Whether or not the primary texture is sRGB or not.
  };

  // Maps Ultralight Texture IDs to OpenGL texture handles
  std::map<uint32_t, TextureEntry> texture_map;
  
  uint32_t next_texture_id_ = 1;
  uint32_t next_render_buffer_id_ = 1; // 0 is reserved for default render buffer
  uint32_t next_geometry_id_ = 1;

  struct GeometryEntry {
    GLuint vao; // VAO id
    GLuint vbo_vertices; // VBO id for vertices
    GLuint vbo_indices; // VBO id for indices
  };
  std::map<uint32_t, GeometryEntry> geometry_map;

  struct RenderBufferEntry {
    GLuint fbo_id = 0; // GL FBO ID (if MSAA is enabled, this will be used for resolve)
    GLuint msaa_fbo_id = 0; // GL FBO ID for MSAA
    bool needs_resolve = false; // Whether or not we need to perform MSAA resolve
    uint32_t texture_id = 0; // The Ultralight texture ID backing this RenderBuffer.
#if ENABLE_OFFSCREEN_GL
    RefPtr<Bitmap> bitmap;
    GLuint pbo_id = 0;
    bool is_bitmap_dirty = false;
    bool is_first_draw = true;
    bool needs_update = false;
#endif
  };

  void ResolveIfNeeded(uint32_t render_buffer_id);

  void MakeTextureSRGBIfNeeded(uint32_t texture_id);

#if ENABLE_OFFSCREEN_GL
  void UpdateBitmap(RenderBufferEntry& entry, GLuint pbo_id);
#endif

  std::map<uint32_t, RenderBufferEntry> render_buffer_map;

  struct ProgramEntry {
    GLuint program_id;
    GLuint vert_shader_id;
    GLuint frag_shader_id;
  };
  std::map<ProgramType, ProgramEntry> programs_;
  GLuint cur_program_id_;

  std::vector<Command> command_list;
  int batch_count_;
};

}  // namespace ultralight
