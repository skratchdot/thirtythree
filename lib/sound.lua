--
-- Sound class
-- contains information for sample
-- includes splicing informatoin
--

Sound={}

function Sound:new(o)
  o=o or {}
  setmetatable(o,self)
  self.__index=self

  -- parameters
  if o.melodic==nil then
    o.melodic=true
  end
  o.id=o.id or 1
  o.group=o.group or 1
  o.s=o.s or 0
  o.e=o.e or 1
  o.rate=o.rate or 1
  o.amp=o.amp or 1
  o.lpf=o.lpf or 20000
  o.hpf=o.hpf or 20
  o.res=o.res or 1
  o.wav=o.wav or nil
  o.loaded=o.loaded or false

  return o
end

function Sound:load(filename)
  self.wav=wav:get(filename)
  -- partition transients
  if (not self.melodic) and self.wav.onsets[self.id]~=nil then
    self.s=self.wav.onsets[self.id][1]
    self.e=self.wav.onsets[self.id][2]
  end

  self.loaded=true

end

function Sound:dump()
  return {
    melodic=self.melodic,
    id=self.id,
    group=self.group,
    s=self.s,
    e=self.e,
    rate=self.rate,
    amp=self.amp,
    lpf=self.lpf,
    hpf=self.hpf,
    res=self.res,
    wav=self.wav,
    loaded=self.loaded,
  }
end

-- Sound:press will play a sound from a sample
function Sound:play(i,override)
  if not self.loaded then
    print("sound not loaded")
    do return end
  end
  local voice=nil
  local s=self.s
  local e=self.e
  local effect=0
  local amp=self.amp
  if override~=nil then
    voice=override.voice
    s=override.s or s
    e=override.e or e
    effect=override.effect or 0
    amp=override.amp or amp
  end
  if voice==nil then
    voice=voices:get(self.group)
  end
  if mode_debug then
    print("playing "..self.wav.name.." on voice "..voice.." at pos ("..s..","..e..")")
    print(voice,-- which sampler player
      self.wav.sc_index,-- buffer number
      effect,
      amp,
      self.rate,
      s,
    e)
  end
  engine.tt_play(
    voice,-- which sampler player
    self.wav.sc_index,-- buffer number
    effect,
    amp,
    self.rate,
    s,
    e
  )
end

function Sound:get_start_end(s,e)
  local s=util.linlin(0,1,0,self.wav.duration,s)
  local e=util.linlin(0,1,0,self.wav.duration,e)
  return s,e
end

return Sound


